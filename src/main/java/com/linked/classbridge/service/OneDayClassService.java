package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.CLASS_NOT_FOUND;

import com.linked.classbridge.domain.Category;
import com.linked.classbridge.domain.ClassFAQ;
import com.linked.classbridge.domain.ClassImage;
import com.linked.classbridge.domain.ClassTag;
import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.oneDayClass.ClassDto;
import com.linked.classbridge.dto.oneDayClass.ClassDto.ClassRequest;
import com.linked.classbridge.dto.oneDayClass.ClassUpdateDto;
import com.linked.classbridge.dto.oneDayClass.DayOfWeekListCreator;
import com.linked.classbridge.dto.oneDayClass.RepeatClassDto;
import com.linked.classbridge.dto.oneDayClass.RepeatClassDto.dayList;
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
import java.util.concurrent.atomic.AtomicInteger;
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
    public ClassDto.ClassResponse registerClass(/* Authentication authentication, */User user,  ClassRequest request,
                                                                                      List<MultipartFile> files)
    {
        // auth 받아서 유저 정보 업데이트 예정.
        OneDayClass oneDayClass = ClassDto.ClassRequest.toEntity(request);
        oneDayClass.setTutor(user);
        Category category = categoryRepository.findByName(request.categoryType());
        oneDayClass.setCategory(category);

        kakaoMapService.extracted(oneDayClass);

        oneDayClass = classRepository.save(oneDayClass);

        AtomicInteger idx = new AtomicInteger(1);
        for(ClassFAQ faq : request.faqList()) {
            faq.setSequence(idx.getAndIncrement());
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
        RepeatClassDto repeatClassDto = request.repeatClassDto();
        if(repeatClassDto.getMon() != null && !repeatClassDto.getMon().getTimes().isEmpty()) {
            addLesson(dayOfWeekListMap, lessonList, repeatClassDto.getMon(), DayOfWeek.MONDAY, oneDayClass);
        }
        if(repeatClassDto.getTue() != null && !repeatClassDto.getTue().getTimes().isEmpty()) {
            addLesson(dayOfWeekListMap, lessonList, repeatClassDto.getTue(), DayOfWeek.TUESDAY, oneDayClass);
        }
        if(repeatClassDto.getWed() != null && !repeatClassDto.getWed().getTimes().isEmpty()) {
            addLesson(dayOfWeekListMap, lessonList, repeatClassDto.getWed(), DayOfWeek.WEDNESDAY, oneDayClass);
        }
        if(repeatClassDto.getThu() != null && !repeatClassDto.getThu().getTimes().isEmpty()) {
            addLesson(dayOfWeekListMap, lessonList, repeatClassDto.getThu(), DayOfWeek.THURSDAY, oneDayClass);
        }
        if(repeatClassDto.getFri() != null && !repeatClassDto.getFri().getTimes().isEmpty()) {
            addLesson(dayOfWeekListMap, lessonList, repeatClassDto.getFri(), DayOfWeek.FRIDAY, oneDayClass);
        }
        if(repeatClassDto.getSat() != null && !repeatClassDto.getSat().getTimes().isEmpty()) {
            addLesson(dayOfWeekListMap, lessonList, repeatClassDto.getSat(), DayOfWeek.SATURDAY, oneDayClass);
        }
        if(repeatClassDto.getSun() != null && !repeatClassDto.getSun().getTimes().isEmpty()) {
            addLesson(dayOfWeekListMap, lessonList, repeatClassDto.getSun(), DayOfWeek.SUNDAY, oneDayClass);
        }
        return lessonList;
    }

    private void addLesson(Map<DayOfWeek, List<LocalDate>> dayOfWeekListMap, List<Lesson> lessonList, dayList dayList,
                           DayOfWeek dayOfWeek, OneDayClass oneDayClass) {
        for(LocalDate date : dayOfWeekListMap.get(dayOfWeek)) {
            for (LocalTime time : dayList.getTimes()) {
                Lesson lesson = new Lesson();
                lesson.setLessonDate(date);
                lesson.setStartTime(time);
                lesson.setEndTime(time.plusMinutes(oneDayClass.getTimeTaken()));
                lesson.setOneDayClass(oneDayClass);
                lesson.setPersonnel(dayList.getPersonal());
                lesson.setParticipantNumber(0);
                lessonList.add(lesson);
            }
        }
    }

    public Page<ClassDto> getOneDayClassList(Pageable pageable) {
        // auth 받아서 유저의 클래스 리스트 반환 -> 추가 예정
        Page<OneDayClass> classList = classRepository.findAllByTutorUserId(1L, pageable);
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
    public ClassUpdateDto.ClassResponse updateClass(ClassUpdateDto.ClassRequest request, long classId) {
        // user 정보와 변경할 class 의 user 정보 비교 추가 예정
        OneDayClass oneDayClass = classRepository.findById(classId).orElseThrow(() -> new RestApiException(CLASS_NOT_FOUND));
        List<Integer> changeImageIdList = new ArrayList<>();

        imageRepository.deleteAllByOneDayClassAndSequenceIn(oneDayClass, changeImageIdList);

        OneDayClass changeClass = ClassUpdateDto.ClassRequest.toEntity(request);
        changeClass.setClassId(classId);
        changeClass.setTotalReviews(oneDayClass.getTotalReviews());
        changeClass.setTotalStarRate(oneDayClass.getTotalStarRate());

        if(!changeClass.getAddress1().equals(oneDayClass.getAddress1())
                || !changeClass.getAddress2().equals(oneDayClass.getAddress2())
                || !changeClass.getAddress3().equals(oneDayClass.getAddress3())) {
            kakaoMapService.extracted(oneDayClass);
        } else {
            changeClass.setLatitude(oneDayClass.getLatitude());
            changeClass.setLongitude(oneDayClass.getLongitude());
        }

        // 만약 시작일 전 수업이 있는 경우 -> 레슨 예약자가 있는 경우, 없는 경우 생각해야 할 듯

        oneDayClass = classRepository.save(changeClass);

        tagRepository.deleteAllByOneDayClassClassId(classId);

        List<ClassTag> classTagList = tagRepository.saveAll(request.tagList());

        oneDayClass.setTagList(classTagList);

        return ClassUpdateDto.ClassResponse.fromEntity(oneDayClass);
    }

    @Transactional
    public boolean deleteClass(long classId) {
        // user 정보와 변경할 class 의 user 정보 비교 추가 예정
        OneDayClass oneDayClass = classRepository.findById(classId).orElseThrow(() -> new RestApiException(CLASS_NOT_FOUND));

        // 아직 수강 완료되지 않은 예약이 존재하는 경우 실패 추가 예정

        lessonRepository.deleteAllByOneDayClassClassIdAndLessonDateIsAfter(oneDayClass.getClassId(), LocalDate.now());
        tagRepository.deleteAllByOneDayClassClassId(classId);
        faqRepository.deleteAllByOneDayClassClassId(classId);

        List<ClassImage> imageList = imageRepository.findAllByOneDayClassClassId(classId);

        for(ClassImage image : imageList) {
            s3Service.delete(image.getUrl());
        }

        imageRepository.deleteAllByOneDayClassClassId(classId);

        return true;
    }

    public ClassDto.ClassResponse getOneDayClass(long classId) {
        // user 정보와 변경할 class 의 user 정보 비교 추가 예정


        OneDayClass oneDayClass = classRepository.findById(classId).orElseThrow(() -> new RestApiException(CLASS_NOT_FOUND));
        return ClassDto.ClassResponse.fromEntity(oneDayClass);
    }

    public OneDayClass findClassById(Long classId) {
        return classRepository.findById(classId)
                .orElseThrow(() -> new RestApiException(ErrorCode.CLASS_NOT_FOUND));
    }
}
