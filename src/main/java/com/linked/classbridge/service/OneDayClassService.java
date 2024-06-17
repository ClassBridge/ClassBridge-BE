package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.CANNOT_CHANGE_END_DATE_CAUSE_RESERVED_PERSON_EXISTS;
import static com.linked.classbridge.type.ErrorCode.CANNOT_CHANGE_START_DATE;
import static com.linked.classbridge.type.ErrorCode.CANNOT_DELETE_CLASS_CAUSE_RESERVED_PERSON_EXISTS;
import static com.linked.classbridge.type.ErrorCode.FAQ_NOT_FOUND;
import static com.linked.classbridge.type.ErrorCode.TAG_NOT_FOUND;
import static com.linked.classbridge.type.ErrorCode.CLASS_HAVE_MAX_FAQ;
import static com.linked.classbridge.type.ErrorCode.CLASS_HAVE_MAX_TAG;
import static com.linked.classbridge.type.ErrorCode.CLASS_NOT_FOUND;
import static com.linked.classbridge.type.ErrorCode.EXISTS_LESSON_DATE_START_TIME;
import static com.linked.classbridge.type.ErrorCode.EXISTS_RESERVED_PERSON;
import static com.linked.classbridge.type.ErrorCode.INVALIDATE_CLASS_INTRODUCTION;
import static com.linked.classbridge.type.ErrorCode.INVALIDATE_CLASS_NAME;
import static com.linked.classbridge.type.ErrorCode.INVALIDATE_CLASS_PERSONAL;
import static com.linked.classbridge.type.ErrorCode.LESSON_DATE_MUST_BE_AFTER_NOW;
import static com.linked.classbridge.type.ErrorCode.LESSON_NOT_FOUND;
import static com.linked.classbridge.type.ErrorCode.MISMATCH_CLASS_FAQ;
import static com.linked.classbridge.type.ErrorCode.MISMATCH_CLASS_LESSON;
import static com.linked.classbridge.type.ErrorCode.MISMATCH_CLASS_TAG;
import static com.linked.classbridge.type.ErrorCode.MISMATCH_USER_CLASS;
import static com.linked.classbridge.type.ErrorCode.MISMATCH_USER_FAQ;
import static com.linked.classbridge.type.ErrorCode.MISMATCH_USER_LESSON;
import static com.linked.classbridge.type.ErrorCode.MISMATCH_USER_TAG;
import static com.linked.classbridge.type.ErrorCode.USER_NOT_FOUND;

import com.linked.classbridge.domain.Category;
import com.linked.classbridge.domain.ClassFAQ;
import com.linked.classbridge.domain.ClassImage;
import com.linked.classbridge.domain.ClassTag;
import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.domain.document.OneDayClassDocument;
import com.linked.classbridge.dto.oneDayClass.ClassDto;
import com.linked.classbridge.dto.oneDayClass.ClassDto.ClassRequest;
import com.linked.classbridge.dto.oneDayClass.ClassFAQDto;
import com.linked.classbridge.dto.oneDayClass.ClassTagDto;
import com.linked.classbridge.dto.oneDayClass.ClassUpdateDto;
import com.linked.classbridge.dto.oneDayClass.DayOfWeekListCreator;
import com.linked.classbridge.dto.oneDayClass.LessonDto;
import com.linked.classbridge.dto.oneDayClass.LessonDto.Request;
import com.linked.classbridge.dto.oneDayClass.RepeatClassDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.CategoryRepository;
import com.linked.classbridge.repository.ClassFAQRepository;
import com.linked.classbridge.repository.ClassImageRepository;
import com.linked.classbridge.repository.ClassTagRepository;
import com.linked.classbridge.repository.LessonRepository;
import com.linked.classbridge.repository.OneDayClassDocumentRepository;
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
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
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
    private final ElasticsearchOperations operations;
    private final OneDayClassDocumentRepository oneDayClassDocumentRepository;

    @Transactional
    public ClassDto.ClassResponse registerClass(String email, ClassRequest request,List<MultipartFile> files)
    {
        User tutor = getUser(email);

        OneDayClass oneDayClass = ClassDto.ClassRequest.toEntity(request);
        oneDayClass.setTutor(tutor);

        Category category = categoryRepository.findByName(request.categoryType());
        oneDayClass.setCategory(category);

        if(oneDayClass.getEndDate() == null) {
            oneDayClass.setEndDate(oneDayClass.getStartDate().plusMonths(3));
        }

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

        oneDayClass.setImageList(imageRepository.saveAll(saveImages(oneDayClass, files)));

        oneDayClass.setLessonList(lessonRepository.saveAll(createRepeatLesson(request, oneDayClass)));

        oneDayClass.setFaqList(faqRepository.saveAll(request.faqList()));

        oneDayClass.setTagList(tagRepository.saveAll(request.tagList()));

        oneDayClass.setImageList(imageRepository.saveAll(saveImages(oneDayClass, files)));

        operations.save(new OneDayClassDocument(oneDayClass));

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
        Map<DayOfWeek, List<LocalDate>> dayOfWeekListMap = DayOfWeekListCreator.createDayOfWeekLists(oneDayClass.getStartDate(), oneDayClass.getEndDate());

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
        User tutor = getUser(email);
        Page<OneDayClass> classList = classRepository.findAllByTutorUserId(tutor.getUserId(), pageable);
        Map<Long, String> imageMap = (classImageRepository.findAllByOneDayClassClassIdInAndSequence(classList.map(OneDayClass::getClassId).toList(), 1))
                .stream().collect(Collectors.toMap(
                        classImage -> classImage.getOneDayClass().getClassId(),
                        ClassImage::getUrl
                ));

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
        OneDayClass oneDayClass = getClass(classId);
        User tutor = getUser(email);
        validateOneDayClassMatchTutor(tutor, oneDayClass);

        OneDayClass changeClass = ClassUpdateDto.ClassRequest.toEntity(request);
        changeClass.setClassId(classId);
        changeClass.setTotalReviews(oneDayClass.getTotalReviews());
        changeClass.setTotalStarRate(oneDayClass.getTotalStarRate());
        changeClass.setTutor(oneDayClass.getTutor());

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

        OneDayClassDocument beforeDocument = oneDayClassDocumentRepository.findById(classId).orElseThrow(() -> new RestApiException(CLASS_NOT_FOUND));

        OneDayClassDocument afterDocument = new OneDayClassDocument(changeClass);
        afterDocument.setImageUrl(beforeDocument.getImageUrl());

        operations.save(afterDocument);

        return ClassUpdateDto.ClassResponse.fromEntity(changeClass);
    }

    @Transactional
    public boolean deleteClass(String email, long classId) {
        OneDayClass oneDayClass = getClass(classId);
        User tutor = getUser(email);
        validateOneDayClassMatchTutor(tutor, oneDayClass);

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

        oneDayClassDocumentRepository.deleteById(classId);

        return true;
    }

    public ClassDto.ClassResponse getOneDayClass(String email, long classId) {
        OneDayClass oneDayClass = getClass(classId);
        User tutor = getUser(email);
        validateOneDayClassMatchTutor(tutor, oneDayClass);

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
        User tutor = getUser(email);
        OneDayClass oneDayClass = getClass(classId);

        if(!Objects.equals(tutor.getUserId(), oneDayClass.getTutor().getUserId())) {
            throw new RestApiException(MISMATCH_USER_CLASS);
        }

        if(tagRepository.findAllByOneDayClassClassId(classId).size() >= 5) {
            throw new RestApiException(CLASS_HAVE_MAX_TAG);
        }

        ClassTag classTag = ClassTag.builder()
                .name(request.getName())
                .oneDayClass(oneDayClass)
                .build();
        ClassTagDto classTagDto = new ClassTagDto(tagRepository.save(classTag));

        OneDayClassDocument oneDayClassDocument = oneDayClassDocumentRepository.findById(classId).orElseThrow(() -> new RestApiException(CLASS_NOT_FOUND));
        List<String> tagList = new ArrayList<>(oneDayClassDocument.getTagList());
        tagList.add(classTag.getName());
        oneDayClassDocument.setTagList(tagList);
        operations.save(oneDayClassDocument);

        return classTagDto;
    }

    public ClassTagDto updateTag(String email, ClassTagDto request, long classId, long tagId) {
        User tutor = getUser(email);
        ClassTag classTag = getTag(tagId);
        validateTagMatchTutorAndClassId(tutor, classId, classTag);

        String beforeTagName = classTag.getName();

        classTag.setName(request.getName());

        ClassTagDto classTagDto = new ClassTagDto(tagRepository.save(classTag));

        OneDayClassDocument oneDayClassDocument = oneDayClassDocumentRepository.findById(classId).orElseThrow(() -> new RestApiException(CLASS_NOT_FOUND));
        List<String> tagList = new ArrayList<>(oneDayClassDocument.getTagList());
        tagList.remove(beforeTagName);
        tagList.add(request.getName());
        oneDayClassDocument.setTagList(tagList);
        operations.save(oneDayClassDocument);

        return classTagDto;
    }

    public Boolean deleteTag(String email, long classId, long tagId) {
        User tutor = getUser(email);
        ClassTag classTag = getTag(tagId);
        validateTagMatchTutorAndClassId(tutor, classId, classTag);

        tagRepository.delete(classTag);

        OneDayClassDocument oneDayClassDocument = oneDayClassDocumentRepository.findById(classId).orElseThrow(() -> new RestApiException(CLASS_NOT_FOUND));
        List<String> tagList = new ArrayList<>(oneDayClassDocument.getTagList());
        tagList.remove(classTag.getName());
        oneDayClassDocument.setTagList(tagList);

        return true;
    }


    private void validateTagMatchTutorAndClassId(User tutor, long classId, ClassTag classTag) {
        if(!Objects.equals(tutor.getUserId(), classTag.getOneDayClass().getTutor().getUserId())) {
            throw new RestApiException(MISMATCH_USER_TAG);
        }

        if(!Objects.equals(classTag.getOneDayClass().getClassId(), classId)) {
            throw new RestApiException(MISMATCH_CLASS_TAG);
        }

    }


    public ClassFAQDto registerFAQ(String email, ClassFAQDto request, long classId) {
        User tutor = getUser(email);
        OneDayClass oneDayClass = getClass(classId);
        validateOneDayClassMatchTutor(tutor, oneDayClass);

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
        User tutor = getUser(email);
        ClassFAQ classFAQ = getFAQ(faqId);

        validateFAQMatchTutorAndClassId(tutor, classId, classFAQ);

        classFAQ.setTitle(request.getTitle());
        classFAQ.setContent(request.getContent());

        return new ClassFAQDto(faqRepository.save(classFAQ));
    }

    public boolean deleteFAQ(String email, long classId, long faqId) {
        User tutor = getUser(email);
        ClassFAQ classFAQ = getFAQ(faqId);

        validateFAQMatchTutorAndClassId(tutor, classId, classFAQ);

        faqRepository.delete(classFAQ);

        return true;
    }

    private void validateFAQMatchTutorAndClassId(User tutor, long classId, ClassFAQ classFAQ) {
        if(!Objects.equals(tutor.getUserId(), classFAQ.getOneDayClass().getTutor().getUserId())) {
            throw new RestApiException(MISMATCH_USER_FAQ);
        }

        if(!Objects.equals(classId, classFAQ.getOneDayClass().getClassId())) {
            throw new RestApiException(MISMATCH_CLASS_FAQ);
        }
    }

    private void validateOneDayClassMatchTutor(User tutor, OneDayClass oneDayClass) {
        if(!Objects.equals(tutor.getUserId(), oneDayClass.getTutor().getUserId())) {
            throw new RestApiException(MISMATCH_USER_CLASS);
        }
    }

    public LessonDto registerLesson(String email, LessonDto.Request request, Long classId) {
        if(lessonRepository.existsByOneDayClassClassIdAndLessonDateAndStartTime(classId, request.lessonDate(), request.startTime())) {
            throw new RestApiException(EXISTS_LESSON_DATE_START_TIME);
        }

        if(request.lessonDate().isEqual(LocalDate.now()) || request.lessonDate().isBefore(LocalDate.now())) {
            throw new RestApiException(LESSON_DATE_MUST_BE_AFTER_NOW);
        }

        OneDayClass oneDayClass = getClass(classId);
        User tutor = getUser(email);

        validateOneDayClassMatchTutor(tutor, oneDayClass);

        return new LessonDto(lessonRepository.save(request.toEntity(oneDayClass)), oneDayClass.getPersonal());
    }

    public Boolean deleteLesson(String email, Long classId, Long lessonId) {
        User tutor = getUser(email);
        Lesson lesson = getLesson(lessonId);

        validateLesson(tutor, classId, lesson);

        lessonRepository.delete(lesson);

        return true;
    }

    public LessonDto updateLesson(String email, Request request, Long classId, Long lessonId) {
        if(request.lessonDate().isEqual(LocalDate.now()) || request.lessonDate().isBefore(LocalDate.now())) {
            throw new RestApiException(LESSON_DATE_MUST_BE_AFTER_NOW);
        }

        if(lessonRepository.existsByOneDayClassClassIdAndLessonDateAndStartTime(classId, request.lessonDate(), request.startTime())) {
            throw new RestApiException(EXISTS_LESSON_DATE_START_TIME);
        }
        User tutor = getUser(email);
        Lesson lesson = getLesson(lessonId);

        validateLesson(tutor, classId, lesson);
        lesson.setLessonDate(request.lessonDate());
        lesson.setStartTime(request.startTime());
        lesson.setEndTime(request.startTime().plusMinutes(lesson.getOneDayClass().getDuration()));

        return new LessonDto(lessonRepository.save(lesson), lesson.getOneDayClass().getPersonal());

    }

    private ClassTag getTag(Long tagId) {
        return tagRepository.findById(tagId).orElseThrow(() -> new RestApiException(TAG_NOT_FOUND));
    }

    private ClassFAQ getFAQ(Long faqId) {
        return faqRepository.findById(faqId).orElseThrow(() -> new RestApiException(FAQ_NOT_FOUND));
    }

    private OneDayClass getClass(Long classId) {
        return classRepository.findById(classId).orElseThrow(() -> new RestApiException(CLASS_NOT_FOUND));
    }

    private Lesson getLesson(Long lessonId) {
        return lessonRepository.findById(lessonId).orElseThrow(() -> new RestApiException(LESSON_NOT_FOUND));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RestApiException(USER_NOT_FOUND));
    }

    private void validateLesson(User tutor, Long classId, Lesson lesson) {

        if(!Objects.equals(lesson.getOneDayClass().getClassId(), classId)) {
            throw new RestApiException(MISMATCH_CLASS_LESSON);
        }

        if(!Objects.equals(lesson.getOneDayClass().getTutor().getUserId(), tutor.getUserId())) {
            throw new RestApiException(MISMATCH_USER_LESSON);
        }

        if(lesson.getLessonDate().isEqual(LocalDate.now()) || lesson.getLessonDate().isBefore(LocalDate.now())) {
            throw new RestApiException(LESSON_DATE_MUST_BE_AFTER_NOW);
        }

        if(lesson.getParticipantNumber() > 0) {
            throw new RestApiException(EXISTS_RESERVED_PERSON);
        }
    }
}
