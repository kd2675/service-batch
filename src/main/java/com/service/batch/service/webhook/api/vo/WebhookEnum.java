package com.service.batch.service.webhook.api.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WebhookEnum {
    COMMAND_100(100L, "$time", "$t", "퇴근까지 남은 시간 알려드립니다."),
    COMMAND_101(101L, "$uptime", "$ut", "출근 후 지난 시간 알려드립니다."),

    COMMAND_200(200L, "$news", "$n", "뉴스 찾아드립니다. ex)$news 밥,세일,탕수육(and조건) 0(pageNo) 10(pagePerCnt<=10)"),
    COMMAND_201(201L, "$oldNews", "$on", "지난 뉴스 찾아드립니다. ex)$oldNews 밥,세일,탕수육(and조건) 0(pageNo) 10(pagePerCnt<=10)"),
    COMMAND_202(202L, "$subscribeNews", "$scn", "뉴스구독 리스트 ex)$scn"),
    COMMAND_203(203L, "$subscribeNewsIns", "$scni", "뉴스구독 추가 ex)$scni 밥,세일,탕수육(and조건), $scn '밥 세일 탕수육'(한줄)"),
    COMMAND_204(204L, "$subscribeNewsDel", "$scnd", "뉴스구독 삭제 ex)$scnd 3(id)"),

    COMMAND_300(300L, "$music", "$m", "전체 리스트 에서 랜덤으로 노래 추천."),
    COMMAND_301(301L, "$musicSearch", "$ms", "노래검색 ex)$musicSearch '노래' 0(pageNo) 10(pagePerCnt<=10)"),
    COMMAND_302(302L, "$musicPlay", "$mp", "뮤직 임베디드 ex)$musicPlay 561(musicSearch 번호)"),
    COMMAND_303(303L, "$playlist", "$pl", "플레이리스트"),
    COMMAND_304(304L, "$playlistAdd", "$pad", "플레이리스트 노래 추가 ex)playlistAdd 321(musicSearch 번호)"),
    COMMAND_305(305L, "$playlistRemove", "$prm", "플레이리스트 노래 삭제 ex)playlistRemove 2(playlist id)"),

    COMMAND_400(400L, "$watch", "$w", "추천 리스트 에서 랜덤으로 영화&미드&애니 추천."),
    COMMAND_401(401L, "$watchList", "$wl", "추천 영화&미드&애니 리스트 ex)$watchList"),
    COMMAND_402(402L, "$watchAdd", "$wad", "추천 영화&미드&애니 리스트 추가 ex)$watchAdd '제목' 10(평점)"),
    COMMAND_403(403L, "$watchY", "$wy", "추천 영화&미드&애니 리스트 봣음 ex)$watchY 2(watchList id)"),
    COMMAND_404(404L, "$watchRemove", "$wrm", "추천 영화&미드&애니 리스트 삭제 ex)$watchRemove 2(watchList id)"),

    COMMAND_500(500L, "$rockScissorsPaper", "$rsp", "!작업중! 가위 바위 보"),

    COMMAND_600(600L, "$hotdealSearch", "$hs", "핫딜 검색 ex)$hs 밥,세일,탕수육(and조건) 0(pageNo) 10(pagePerCnt<=10)"),
    COMMAND_601(601L, "$hotdealSearchApi", "$hsa", "핫딜 API 검색 ex)$hsa 밥,세일,탕수육(and조건) 0(pageNo)"),
    COMMAND_602(602L, "$hotdealAlimIns", "$hi", "핫딜 키워드알림 추가 ex)$hi 밥 호(용, 우, 김, 주, 갑, 뀨)"),
    COMMAND_603(603L, "$hotdealAlimDel", "$hd", "핫딜 키워드알림 삭제 ex)$hd 123(id)"),
    COMMAND_604(604L, "$hotdealAlimList", "$hl", "핫딜 키워드알림 리스트 출력 ex)$hl 호(용, 우, 김, 주, 갑, 뀨)"),

    COMMAND(9999L, "$command", "$c", "명령어 설명");

    private Long id;
    private String key;
    private String shortKey;
    private String value;
}
