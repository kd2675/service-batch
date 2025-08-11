package com.service.batch.service.webhook.api.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberEnum {
    YONG(1L, "용", "ewtmwoy8ridump183qucqgtb5y", "kimdo", "3qzts33zz3gqtefxkwjhc3q8jw"),
    WOO(2L, "우", "i5xo5tnf67diidscisppku3u3h", "klmd0", "3ct9nc6jtpnqmdoiq79pf5osqy"),
    HO(3L, "호", "3rgampx9pbr5fjjizq97mjyb9y", "kimd0.", "mopmktyqitb99pe9nifjdte8ta"),
    KIM(4L, "김", "irzi4hmjb781ppfd1nrpfm8ruw", "kimd0", "6fombw5e8ifmuqgbzgpwhnfgne"),
    JOO(5L, "주", "46h1ubq517dff8x6ifr1bhwcie", "kimd0young", "ipym1z4wo3nc3eahdcehhnirpo"),
    GAP(6L, "갑", "hy4ynqft37fmudw1pjdx14uu9y", "kappa", "x5c5bzk9qfrjtrtcmay3sz8fpr"),
    SYSTEM(7L, "시스템", "hbdk91y6jprwuxr5a7xyd6s3uh", "system", "6fombw5e8ifmuqgbzgpwhnfgne"),
    gyu(8L, "뀨", "hkf1r5tohiyaidca6qjz64z4fc", "gyuhyolee", "w3s55e3eofnk7ex8h1of1cmyue");

    private Long id;
    private String target;
    private String userTokenId;
    private String userId;
    private String directChannelId;
}
