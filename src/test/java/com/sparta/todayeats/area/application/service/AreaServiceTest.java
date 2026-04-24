package com.sparta.todayeats.area.application.service;

import com.sparta.todayeats.area.domain.entity.Area;
import com.sparta.todayeats.area.domain.repository.AreaRepository;
import com.sparta.todayeats.area.presentation.dto.AreaCreateRequest;
import com.sparta.todayeats.area.presentation.dto.AreaCreateResponse;
import com.sparta.todayeats.area.presentation.dto.AreaResponse;
import com.sparta.todayeats.area.presentation.dto.AreaUpdateRequest;
import com.sparta.todayeats.global.response.PageResponse;
import com.sparta.todayeats.global.exception.BaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AreaServiceTest {

    @InjectMocks
    private AreaService areaService;

    @Mock
    private AreaRepository areaRepository;


    // 운영 지역 생성
    @Nested
    @DisplayName("운영 지역 생성")
    class CreateArea {

        @Test
        void 운영지역_생성_성공() {
            // given
            AreaCreateRequest request = new AreaCreateRequest("이태원", "서울특별시", "용산구");

            given(areaRepository.existsByNameIgnoreCase("이태원"))
                    .willReturn(false);

            given(areaRepository.save(any(Area.class)))
                    .willAnswer(invocation -> {
                        Area a = invocation.getArgument(0);
                        return Area.builder()
                                .id(UUID.randomUUID())
                                .name(a.getName())
                                .city(a.getCity())
                                .district(a.getDistrict())
                                .isActive(true)
                                .build();
                    });

            // when
            AreaCreateResponse result = areaService.createArea(request);

            // then
            assertThat(result.getName()).isEqualTo("이태원");
            assertThat(result.getCity()).isEqualTo("서울특별시");
            assertThat(result.getDistrict()).isEqualTo("용산구");
            assertThat(result.getIsActive()).isTrue();
        }

        @Test
        void 운영지역_이름이_중복이면_예외발생() {
            // given
            AreaCreateRequest request = new AreaCreateRequest("이태원", "서울특별시", "용산구");

            given(areaRepository.existsByNameIgnoreCase("이태원"))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() ->
                    areaService.createArea(request)
            ).isInstanceOf(BaseException.class);
        }

        @Test
        void 운영지역_이름_앞뒤공백은_제거후_저장() {
            // given
            AreaCreateRequest request = new AreaCreateRequest("  이태원  ", "서울특별시", "용산구");

            given(areaRepository.existsByNameIgnoreCase("이태원"))
                    .willReturn(false);

            given(areaRepository.save(any(Area.class)))
                    .willAnswer(invocation -> {
                        Area a = invocation.getArgument(0);
                        return Area.builder()
                                .id(UUID.randomUUID())
                                .name(a.getName())
                                .city(a.getCity())
                                .district(a.getDistrict())
                                .isActive(true)
                                .build();
                    });

            // when
            AreaCreateResponse result = areaService.createArea(request);

            // then
            assertThat(result.getName()).isEqualTo("이태원"); // trim 됐는지 확인
        }
    }


    // 운영 지역 목록 조회 및 검색
    @Nested
    @DisplayName("운영 지역 목록 조회")
    class GetAreas {

        @Test
        void 운영지역_전체조회_성공() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            Area area = Area.builder()
                    .id(UUID.randomUUID())
                    .name("이태원")
                    .city("서울특별시")
                    .district("용산구")
                    .isActive(true)
                    .build();

            Page<Area> pageResult = new PageImpl<>(List.of(area), pageable, 1);

            given(areaRepository.findAll(pageable))
                    .willReturn(pageResult);

            // when
            PageResponse<AreaResponse> result = areaService.getAreas(null, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("이태원");
        }

        @Test
        void 운영지역_이름으로_검색조회_성공() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            Area area = Area.builder()
                    .id(UUID.randomUUID())
                    .name("이태원")
                    .city("서울특별시")
                    .district("용산구")
                    .isActive(true)
                    .build();

            Page<Area> pageResult = new PageImpl<>(List.of(area), pageable, 1);

            given(areaRepository.findByNameContainingIgnoreCase("이태", pageable))
                    .willReturn(pageResult);

            // when
            PageResponse<AreaResponse> result = areaService.getAreas("이태", pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("이태원");
        }
    }


    // 운영 지역 단건 조회
    @Nested
    @DisplayName("운영 지역 단건 조회")
    class GetArea {

        @Test
        void 운영지역_단건조회_성공() {
            // given
            UUID id = UUID.randomUUID();

            Area area = Area.builder()
                    .id(id)
                    .name("이태원")
                    .city("서울특별시")
                    .district("용산구")
                    .isActive(true)
                    .build();

            given(areaRepository.findById(id))
                    .willReturn(Optional.of(area));

            // when
            AreaResponse result = areaService.getArea(id);

            // then
            assertThat(result.getName()).isEqualTo("이태원");
            assertThat(result.getCity()).isEqualTo("서울특별시");
        }

        @Test
        void 운영지역가_존재하지_않으면_예외발생() {
            // given
            UUID id = UUID.randomUUID();

            given(areaRepository.findById(id))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    areaService.getArea(id)
            ).isInstanceOf(BaseException.class);
        }
    }


    // 운영 지역 수정
    @Nested
    @DisplayName("운영 지역 수정")
    class UpdateArea {

        @Test
        void 운영지역_수정_성공() {
            // given
            UUID id = UUID.randomUUID();

            Area area = Area.builder()
                    .id(id)
                    .name("이태원")
                    .city("서울특별시")
                    .district("용산구")
                    .isActive(true)
                    .build();

            given(areaRepository.findById(id))
                    .willReturn(Optional.of(area));

            AreaUpdateRequest request = new AreaUpdateRequest("광화문", "서울특별시", "종로구", true);

            // when
            AreaResponse result = areaService.updateArea(id, request);

            // then
            assertThat(result.getName()).isEqualTo("광화문");
            assertThat(result.getDistrict()).isEqualTo("종로구");
        }

        @Test
        void 운영지역_수정시_이름이_중복이면_예외발생() {
            // given
            UUID id = UUID.randomUUID();

            Area area = Area.builder()
                    .id(id)
                    .name("이태원")
                    .city("서울특별시")
                    .district("용산구")
                    .isActive(true)
                    .build();

            given(areaRepository.findById(id))
                    .willReturn(Optional.of(area));

            given(areaRepository.existsByNameIgnoreCase("광화문"))
                    .willReturn(true);

            AreaUpdateRequest request = new AreaUpdateRequest("광화문", "서울특별시", "종로구", true);

            // when & then
            assertThatThrownBy(() ->
                    areaService.updateArea(id, request)
            ).isInstanceOf(BaseException.class);
        }

        @Test
        void 운영지역_수정시_이름이_같으면_중복검증_스킵() {
            // given
            UUID id = UUID.randomUUID();

            Area area = Area.builder()
                    .id(id)
                    .name("이태원")
                    .city("서울특별시")
                    .district("용산구")
                    .isActive(true)
                    .build();

            given(areaRepository.findById(id))
                    .willReturn(Optional.of(area));

            // 이름 동일 → existsByNameIgnoreCase 호출 안 됨
            AreaUpdateRequest request = new AreaUpdateRequest("이태원", "서울특별시", "용산구", false);

            // when
            AreaResponse result = areaService.updateArea(id, request);

            // then
            assertThat(result.getName()).isEqualTo("이태원");
            assertThat(result.getIsActive()).isFalse(); // isActive만 변경됨
        }

        @Test
        void 운영지역_수정시_존재하지_않으면_예외발생() {
            // given
            UUID id = UUID.randomUUID();

            given(areaRepository.findById(id))
                    .willReturn(Optional.empty());

            AreaUpdateRequest request = new AreaUpdateRequest("광화문", "서울특별시", "종로구", true);

            // when & then
            assertThatThrownBy(() ->
                    areaService.updateArea(id, request)
            ).isInstanceOf(BaseException.class);
        }
    }


    // 운영 지역 삭제
    @Nested
    @DisplayName("운영 지역 삭제")
    class DeleteArea {

        @Test
        void 운영지역_삭제_성공() {
            // given
            UUID id = UUID.randomUUID();

            Area area = Area.builder()
                    .id(id)
                    .name("이태원")
                    .city("서울특별시")
                    .district("용산구")
                    .isActive(true)
                    .build();

            given(areaRepository.findById(id))
                    .willReturn(Optional.of(area));

            // when
            areaService.deleteArea(id);

            // then
            assertThat(area.isDeleted()).isTrue();
        }

        @Test
        void 운영지역_삭제시_존재하지_않으면_예외발생() {
            // given
            UUID id = UUID.randomUUID();

            given(areaRepository.findById(id))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    areaService.deleteArea(id)
            ).isInstanceOf(BaseException.class);
        }
    }
}
