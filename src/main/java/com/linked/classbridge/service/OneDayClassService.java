package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.CANNOT_CHANGE_END_DATE_CAUSE_RESERVED_PERSON_EXISTS;
import static com.linked.classbridge.type.ErrorCode.CANNOT_CHANGE_START_DATE;
import static com.linked.classbridge.type.ErrorCode.CANNOT_DELETE_CLASS_CAUSE_RESERVED_PERSON_EXISTS;
import static com.linked.classbridge.type.ErrorCode.CANNOT_FOUND_FAQ;
import static com.linked.classbridge.type.ErrorCode.CANNOT_FOUND_TAG;
import static com.linked.classbridge.type.ErrorCode.CLASS_HAVE_MAX_FAQ;
import static com.linked.classbridge.type.ErrorCode.CLASS_HAVE_MAX_TAG;
import static com.linked.classbridge.type.ErrorCode.CLASS_NOT_FOUND;
import static com.linked.classbridge.type.ErrorCode.INVALIDATE_CLASS_INTRODUCTION;
import static com.linked.classbridge.type.ErrorCode.INVALIDATE_CLASS_NAME;
import static com.linked.classbridge.type.ErrorCode.INVALIDATE_CLASS_PERSONAL;
import static com.linked.classbridge.type.ErrorCode.MISMATCH_CLASS_FAQ;
import static com.linked.classbridge.type.ErrorCode.MISMATCH_CLASS_TAG;
import static com.linked.classbridge.type.ErrorCode.MISMATCH_USER_CLASS;
import static com.linked.classbridge.type.ErrorCode.MISMATCH_USER_FAQ;
import static com.linked.classbridge.type.ErrorCode.MISMATCH_USER_TAG;
import static com.linked.classbridge.type.ErrorCode.USER_NOT_FOUND;

import com.linked.classbridge.domain.Category;
import com.linked.classbridge.domain.ClassFAQ;
import com.linked.classbridge.domain.ClassImage;
import com.linked.classbridge.domain.ClassTag;
import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.oneDayClass.ClassDto;
import com.linked.classbridge.dto.oneDayClass.ClassDto.ClassRequest;
import com.linked.classbridge.dto.oneDayClass.ClassFAQDto;
import com.linked.classbridge.dto.oneDayClass.ClassTagDto;
import com.linked.classbridge.dto.oneDayClass.ClassUpdateDto;
import com.linked.classbridge.dto.oneDayClass.DayOfWeekListCreator;
import com.linked.classbridge.dto.oneDayClass.RepeatClassDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.CategoryRepository;
import com.linked.classbridge.repository.ClassFAQRepository;
import com.linked.classbridge.repository.ClassImageRepository;
import com.linked.classbridge.repository.ClassTagRepository;
import com.linked.classbridge.repository.LessonRepository;
import com.linked.classbridge.repository.OneDayClassRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.type.ErrorCode;
import jakarta.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class OneDayClassService {

    private final OneDayClassRepository classRepository;
    private final UserRepository userRepository;
    private final KakaoMapService kakaoMapService;
    private final ClassTagRepository tagRepository;
    private final ClassFAQRepository faqRepository;
    private final ClassImageRepository imageRepository;
    private final CategoryRepository categoryRepository;
    private final S3Service s3Service;
    private final LessonRepository lessonRepository;
    private final ClassImageRepository classImageRepository;

    @Transactional
    public ClassDto.ClassResponse registerClass(String email, ClassRequest request,List<MultipartFile> files)
    {
        User tutor = userRepository.findByEmail(email).orElseThrow(() -> new RestApiException(USER_NOT_FOUND));
        OneDayClass oneDayClass = ClassDto.ClassRequest.toEntity(request);
        oneDayClass.setTutor(tutor);
        Category category = categoryRepository.findByName(request.categoryType());
        oneDayClass.setCategory(category);

        validateClassName(oneDayClass.getClassName());
        validateClassIntroduction(oneDayClass.getIntroduction());

        kakaoMapService.extracted(oneDayClass);

        oneDayClass = classRepository.save(oneDayClass);

        for(ClassFAQ faq : request.faqList()) {
            faq.setOneDayClass(oneDayClass);
        }

        for(ClassTag tag : request.tagList()) {
            tag.setOneDayClass(oneDayClass);
        }

        oneDayClass.setLessonList(lessonRepository.saveAll(createRepeatLesson(request, oneDayClass)));

        oneDayClass.setFaqList(faqRepository.saveAll(request.faqList()));

        oneDayClass.setTagList(tagRepository.saveAll(request.tagList()));

        oneDayClass.setImageList(imageRepository.saveAll(saveImages(oneDayClass, files)));
        return ClassDto.ClassResponse.fromEntity(oneDayClass);
    }

    private void validateClassName(String className) {
        if (className == null || className.length() > 20 || className.length() < 2) {
            throw new RestApiException(INVALIDATE_CLASS_NAME);
        }
    }

    private void validateClassIntroduction(String classIntroduction) {
        if (classIntroduction == null || classIntroduction.length() > 500 || classIntroduction.length() < 20) {
            throw new RestApiException(INVALIDATE_CLASS_INTRODUCTION);
        }
    }

    private List<ClassImage> saveImages(OneDayClass oneDayClass, List<MultipartFile> files) {
        List<ClassImage> images = new ArrayList<>();
        int sequence = 1;
        for(MultipartFile file : files) {
            String url = s3Service.uploadOneDayClassImage(file);
            images.add(ClassImage.builder()
                    .url(url)
                    .name(file.getOriginalFilename())
                    .sequence(sequence++)
                    .oneDayClass(oneDayClass)
                    .build());
        }
        return !images.isEmpty() ? classImageRepository.saveAll(images) : new ArrayList<>();
    }

    private List<Lesson> createRepeatLesson(ClassRequest request, OneDayClass oneDayClass) {
        Map<DayOfWeek, List<LocalDate>> dayOfWeekListMap = DayOfWeekListCreator.createDayOfWeekLists(request.startDate(), request.endDate());

        List<Lesson> lessonList = new ArrayList<>();
        for(RepeatClassDto repeatClassDto : request.lesson()) {
            addLesson(dayOfWeekListMap, lessonList, repeatClassDto, oneDayClass);
        }

        return lessonList;
    }

    private void addLesson(Map<DayOfWeek, List<LocalDate>> dayOfWeekListMap, List<Lesson> lessonList,
                           RepeatClassDto repeatClassDto, OneDayClass oneDayClass) {
        for(LocalDate date : dayOfWeekListMap.get(repeatClassDto.getDayOfWeek())) {
            for (LocalTime time : repeatClassDto.getTimes()) {
                Lesson lesson = new Lesson();
                lesson.setLessonDate(date);
                lesson.setStartTime(time);
                lesson.setEndTime(time.plusMinutes(oneDayClass.getDuration()));
                lesson.setOneDayClass(oneDayClass);
                lesson.setParticipantNumber(0);
                lessonList.add(lesson);
            }
        }
    }

    public Page<ClassDto> getOneDayClassList(String email, Pageable pageable) {
        User tutor = userRepository.findByEmail(email).orElseThrow(() -> new RestApiException(USER_NOT_FOUND));
        Page<OneDayClass> classList = classRepository.findAllByTutorUserId(tutor.getUserId(), pageable);
        Map<Long, String> imageMap = (classImageRepository.findAllByOneDayClassClassIdInAndSequence(classList.map(OneDayClass::getClassId).toList(), 1))
                .stream().collect(Collectors.toMap(ClassImage::getClassImageId, ClassImage::getUrl));

        Page<ClassDto> classDtoPage = classList.map(ClassDto::new);
        classDtoPage.forEach(item -> {
            if(imageMap.containsKey(item.getClassId())) {
                item.setClassImageUrl(imageMap.get(item.getClassId()));
            }
        });
        return classDtoPage;
    }

    @Transactional
    public ClassUpdateDto.ClassResponse updateClass(String email, ClassUpdateDto.ClassRequest request, long classId) {
        User tutor = userRepository.findByEmail(email).orElseThrow(() -> new RestApiException(USER_NOT_FOUND));
        OneDayClass oneDayClass = classRepository.findById(classId).orElseThrow(() -> new RestApiException(CLASS_NOT_FOUND));

        if(!Objects.equals(tutor.getUserId(), oneDayClass.getTutor().getUserId())) {
            throw new RestApiException(MISMATCH_USER_CLASS);
        }

        OneDayClass changeClass = ClassUpdateDto.ClassRequest.toEntity(request);
        changeClass.setClassId(classId);
        changeClass.setTotalReviews(oneDayClass.getTotalReviews());
        changeClass.setTotalStarRate(oneDayClass.getTotalStarRate());
        changeClass.setTutor(tutor);

        changeClass.setCategory(categoryRepository.findByName(request.categoryType()));

        validateClassName(changeClass.getClassName());
        validateClassIntroduction(changeClass.getIntroduction());

        // 최대 인원이 1보다 작거나 변경할 최대 인원이 현재 날짜 이후에 예약된 사람보다 작은 경우 변경 불가
        if(changeClass.getPersonal() < oneDayClass.getPersonal() &&
                (changeClass.getPersonal() < 1
                || lessonRepository.existsByOneDayClassClassIdAndParticipantNumberIsGreaterThanAndLessonDateIsAfter(classId, changeClass.getPersonal(), LocalDate.now()))) {
            throw new RestApiException(INVALIDATE_CLASS_PERSONAL);
        }

        // 시작일이 변경된 경우
        if(!oneDayClass.getStartDate().isEqual(changeClass.getStartDate())) {
            // 기존 시작일이 현재 날짜 이전인 경우 변경 불가
            if(oneDayClass.getStartDate().isBefore(LocalDate.now())) {
                throw new RestApiException(CANNOT_CHANGE_START_DATE);
            }

            // 변경된 시작일이 현재 날짜 이전인 경우 변경 불가
            if(changeClass.getStartDate().isBefore(LocalDate.now())) {
                throw new RestApiException(CANNOT_CHANGE_START_DATE);
            }

            // 기존 시작일이 변경된 시작일보다 이전이고 기존 시작일과 변경된 시작일 사이의 레슨 중 예약된 사람이 존재하는 경우 변경 불가
            if(oneDayClass.getStartDate().isBefore(changeClass.getStartDate()) &&
                    lessonRepository.existsByOneDayClassClassIdAndLessonDateIsBetweenAndParticipantNumberIsGreaterThan(
                            classId, oneDayClass.getStartDate(), changeClass.getStartDate(), 0)) {
                    throw new RestApiException(CANNOT_CHANGE_START_DATE);
            }

            // 기존 시작일이 변경된 시작일보다 이전이고 기존 시작일과 변경된 시작일 사이의 레슨 중 예약된 사람이 존재하지 않는 경우 해당 기간 사이의 레슨 삭제
            lessonRepository.deleteAllByOneDayClassClassIdAndLessonDateIsBefore(classId, changeClass.getStartDate());
        }

        // 종료일 수정한 경우
        if(!oneDayClass.getEndDate().isEqual(changeClass.getEndDate())) {
            // 해당 종료일 이후에 레슨에 예약된 인원이 있는 경우
            if(lessonRepository.existsByOneDayClassClassIdAndLessonDateIsAfterAndParticipantNumberIsGreaterThan(changeClass.getClassId(), changeClass.getEndDate(), 0)) {
                throw new RestApiException(CANNOT_CHANGE_END_DATE_CAUSE_RESERVED_PERSON_EXISTS);
            } else {
                // 변경일 이후 lesson 은 삭제처리
                lessonRepository.deleteAllByOneDayClassClassIdAndLessonDateIsAfter(classId, changeClass.getEndDate());
            }
        }

        // 주소를 수정한 경우
        if(!changeClass.getAddress1().equals(oneDayClass.getAddress1())
                || !changeClass.getAddress2().equals(oneDayClass.getAddress2())
                || !changeClass.getAddress3().equals(oneDayClass.getAddress3())) {
            kakaoMapService.extracted(oneDayClass);
        } else {
            changeClass.setLatitude(oneDayClass.getLatitude());
            changeClass.setLongitude(oneDayClass.getLongitude());
        }

        changeClass = classRepository.save(changeClass);

        // 소요시간이 변경 된 경우 현재 날짜 이후의 모든 레슨의 종료 시간 변경
        if(oneDayClass.getDuration() != changeClass.getDuration()) {
            List<Lesson> lessonList = lessonRepository.findAllByOneDayClassClassIdAndLessonDateIsAfter(classId,
                    LocalDate.now().minusDays(1));

            for (Lesson lesson : lessonList) {
                lesson.setEndTime(lesson.getEndTime()
                        .plusMinutes((long) (oneDayClass.getDuration() - changeClass.getDuration())));
            }
            lessonRepository.saveAll(lessonList);
        }
        return ClassUpdateDto.ClassResponse.fromEntity(changeClass);
    }

    @Transactional
    public boolean deleteClass(String email, long classId) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RestApiException(USER_NOT_FOUND));
        OneDayClass oneDayClass = classRepository.findById(classId).orElseThrow(() -> new RestApiException(CLASS_NOT_FOUND));

        if(!Objects.equals(oneDayClass.getTutor().getUserId(), user.getUserId())) {
            throw new RestApiException(MISMATCH_USER_CLASS);
        }

        // 현재 날짜 이후의 레슨 중 예약한 사람이 있는 경우 삭제 불가
        if(lessonRepository.existsByOneDayClassClassIdAndLessonDateIsAfterAndParticipantNumberIsGreaterThan(classId, LocalDate.now().minusDays(1), 0)) {
            throw new RestApiException(CANNOT_DELETE_CLASS_CAUSE_RESERVED_PERSON_EXISTS);
        }

        lessonRepository.deleteAllByOneDayClassClassIdAndLessonDateIsAfter(oneDayClass.getClassId(), LocalDate.now());
        tagRepository.deleteAllByOneDayClassClassId(classId);
        faqRepository.deleteAllByOneDayClassClassId(classId);

        List<ClassImage> imageList = imageRepository.findAllByOneDayClassClassId(classId);

        for(ClassImage image : imageList) {
            s3Service.delete(image.getUrl());
        }

        imageRepository.deleteAllByOneDayClassClassId(classId);

        classRepository.deleteById(classId);

        return true;
    }

    public ClassDto.ClassResponse getOneDayClass(String email, long classId) {
        // user 정보와 변경할 class 의 user 정보 비교 추가 예정
        User tutor = userRepository.findByEmail(email).orElseThrow(() -> new RestApiException(USER_NOT_FOUND));
        OneDayClass oneDayClass = classRepository.findById(classId).orElseThrow(() -> new RestApiException(CLASS_NOT_FOUND));

        if(!Objects.equals(tutor.getUserId(), oneDayClass.getTutor().getUserId())) {
            throw new RestApiException(MISMATCH_USER_CLASS);
        }

        oneDayClass.setLessonList(lessonRepository.findAllByOneDayClassClassId(classId));
        oneDayClass.setTagList(tagRepository.findAllByOneDayClassClassId(classId));
        oneDayClass.setFaqList(faqRepository.findAllByOneDayClassClassId(classId));
        oneDayClass.setImageList(imageRepository.findAllByOneDayClassClassId(classId));

        return ClassDto.ClassResponse.fromEntity(oneDayClass);
    }

    public OneDayClass findClassById(Long classId) {
        return classRepository.findById(classId)
                .orElseThrow(() -> new RestApiException(ErrorCode.CLASS_NOT_FOUND));
    }

    public ClassTagDto registerTag(String email, ClassTagDto request, long classId) {
        User tutor = userRepository.findByEmail(email).orElseThrow(() -> new RestApiException(USER_NOT_FOUND));
        OneDayClass oneDayClass = classRepository.findById(classId).orElseThrow(() -> new RestApiException(CLASS_NOT_FOUND));

        if(!Objects.equals(tutor.getUserId(), oneDayClass.getTutor().getUserId())) {
            throw new RestApiException(MISMATCH_USER_CLASS);
        }

        if(tagRepository.findALlByOneDayClassClassId(classId).size() >= 5) {
            throw new RestApiException(CLASS_HAVE_MAX_TAG);
        }

        ClassTag classTag = ClassTag.builder()
                .name(request.getName())
                .oneDayClass(oneDayClass)
                .build();

        return new ClassTagDto(tagRepository.save(classTag));
    }

    public ClassTagDto updateTag(String email, ClassTagDto request, long classId, long tagId) {
        ClassTag classTag = validateTag(email, classId, tagId);

        classTag.setName(request.getName());

        return new ClassTagDto(tagRepository.save(classTag));
    }

    public Boolean deleteTag(String email, long classId, long tagId) {
        ClassTag classTag = validateTag(email, classId, tagId);

        tagRepository.delete(classTag);

        return true;
    }


    private ClassTag validateTag(String email, long classId, long tagId) {
        User tutor = userRepository.findByEmail(email).orElseThrow(() -> new RestApiException(USER_NOT_FOUND));
        ClassTag classTag = tagRepository.findById(tagId).orElseThrow(() -> new RestApiException(CANNOT_FOUND_TAG));

        if(!Objects.equals(tutor.getUserId(), classTag.getOneDayClass().getTutor().getUserId())) {
            throw new RestApiException(MISMATCH_USER_TAG);
        }

        if(!Objects.equals(classTag.getOneDayClass().getClassId(), classId)) {
            throw new RestApiException(MISMATCH_CLASS_TAG);
        }

        return classTag;
    }


    public ClassFAQDto registerFAQ(String email, ClassFAQDto request, long classId) {
        User tutor = userRepository.findByEmail(email).orElseThrow(() -> new RestApiException(USER_NOT_FOUND));
        OneDayClass oneDayClass = classRepository.findById(classId).orElseThrow(() -> new RestApiException(CLASS_NOT_FOUND));

        if(!Objects.equals(tutor.getUserId(), oneDayClass.getTutor().getUserId())) {
            throw new RestApiException(MISMATCH_USER_CLASS);
        }

        if(faqRepository.findAllByOneDayClassClassId(classId).size() >=5) {
            throw new RestApiException(CLASS_HAVE_MAX_FAQ);
        }

        ClassFAQ faq = ClassFAQ.builder().
                title(request.getTitle())
                .content(request.getContent())
                .oneDayClass(oneDayClass)
                .build();

        faq = faqRepository.save(faq);

        return new ClassFAQDto(faq);
    }

    public ClassFAQDto updateFAQ(String email, ClassFAQDto request, long classId, long faqId) {
        ClassFAQ classFAQ = validateFAQ(email, classId, faqId);

        classFAQ.setTitle(request.getTitle());
        classFAQ.setContent(request.getContent());

        return new ClassFAQDto(faqRepository.save(classFAQ));
    }

    public boolean deleteFAQ(String email, long classId, long faqId) {
        ClassFAQ classFAQ = validateFAQ(email, classId, faqId);

        faqRepository.delete(classFAQ);

        return true;
    }

    private ClassFAQ validateFAQ(String email, long classId, long faqId) {
        ClassFAQ classFAQ = faqRepository.findById(faqId).orElseThrow(() -> new RestApiException(CANNOT_FOUND_FAQ));
        User tutor = userRepository.findByEmail(email).orElseThrow(() -> new RestApiException(USER_NOT_FOUND));

        if(!Objects.equals(tutor.getUserId(), classFAQ.getOneDayClass().getTutor().getUserId())) {
            throw new RestApiException(MISMATCH_USER_FAQ);
        }

        if(!Objects.equals(classId, classFAQ.getOneDayClass().getClassId())) {
            throw new RestApiException(MISMATCH_CLASS_FAQ);
        }

        return classFAQ;
    }

}
