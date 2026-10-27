package com.hoabh.chanko.presentation.designsystem.icon

import androidx.annotation.DrawableRes
import com.hoabh.chanko.R

/**
 * 66 Icon IDs thuộc bộ Candy Duotone v2.0-r1 của Chanko.
 * Mỗi ID ánh xạ trực tiếp đến 3 biến thể tài nguyên: normal (default), selected, và disabled.
 */
enum class ChankoIconId(
    @get:DrawableRes val normal: Int,
    @get:DrawableRes val selected: Int,
    @get:DrawableRes val disabled: Int,
) {
    // Navigation (4 IDs)
    NAV_01(R.drawable.chanko_nav_01_default, R.drawable.chanko_nav_01_selected, R.drawable.chanko_nav_01_disabled),
    NAV_02(R.drawable.chanko_nav_02_default, R.drawable.chanko_nav_02_selected, R.drawable.chanko_nav_02_disabled),
    NAV_03(R.drawable.chanko_nav_03_default, R.drawable.chanko_nav_03_selected, R.drawable.chanko_nav_03_disabled),
    NAV_04(R.drawable.chanko_nav_04_default, R.drawable.chanko_nav_04_selected, R.drawable.chanko_nav_04_disabled),

    // Actions (20 IDs)
    ACT_01(R.drawable.chanko_act_01_default, R.drawable.chanko_act_01_selected, R.drawable.chanko_act_01_disabled),
    ACT_02(R.drawable.chanko_act_02_default, R.drawable.chanko_act_02_selected, R.drawable.chanko_act_02_disabled),
    ACT_03(R.drawable.chanko_act_03_default, R.drawable.chanko_act_03_selected, R.drawable.chanko_act_03_disabled),
    ACT_04(R.drawable.chanko_act_04_default, R.drawable.chanko_act_04_selected, R.drawable.chanko_act_04_disabled),
    ACT_05(R.drawable.chanko_act_05_default, R.drawable.chanko_act_05_selected, R.drawable.chanko_act_05_disabled),
    ACT_06(R.drawable.chanko_act_06_default, R.drawable.chanko_act_06_selected, R.drawable.chanko_act_06_disabled),
    ACT_07(R.drawable.chanko_act_07_default, R.drawable.chanko_act_07_selected, R.drawable.chanko_act_07_disabled),
    ACT_08(R.drawable.chanko_act_08_default, R.drawable.chanko_act_08_selected, R.drawable.chanko_act_08_disabled),
    ACT_09(R.drawable.chanko_act_09_default, R.drawable.chanko_act_09_selected, R.drawable.chanko_act_09_disabled),
    ACT_10(R.drawable.chanko_act_10_default, R.drawable.chanko_act_10_selected, R.drawable.chanko_act_10_disabled),
    ACT_11(R.drawable.chanko_act_11_default, R.drawable.chanko_act_11_selected, R.drawable.chanko_act_11_disabled),
    ACT_12(R.drawable.chanko_act_12_default, R.drawable.chanko_act_12_selected, R.drawable.chanko_act_12_disabled),
    ACT_13(R.drawable.chanko_act_13_default, R.drawable.chanko_act_13_selected, R.drawable.chanko_act_13_disabled),
    ACT_14(R.drawable.chanko_act_14_default, R.drawable.chanko_act_14_selected, R.drawable.chanko_act_14_disabled),
    ACT_15(R.drawable.chanko_act_15_default, R.drawable.chanko_act_15_selected, R.drawable.chanko_act_15_disabled),
    ACT_16(R.drawable.chanko_act_16_default, R.drawable.chanko_act_16_selected, R.drawable.chanko_act_16_disabled),
    ACT_17(R.drawable.chanko_act_17_default, R.drawable.chanko_act_17_selected, R.drawable.chanko_act_17_disabled),
    ACT_18(R.drawable.chanko_act_18_default, R.drawable.chanko_act_18_selected, R.drawable.chanko_act_18_disabled),
    ACT_19(R.drawable.chanko_act_19_default, R.drawable.chanko_act_19_selected, R.drawable.chanko_act_19_disabled),
    ACT_20(R.drawable.chanko_act_20_default, R.drawable.chanko_act_20_selected, R.drawable.chanko_act_20_disabled),

    // Items & Cooldown & Verdicts (13 IDs)
    ITM_01(R.drawable.chanko_itm_01_default, R.drawable.chanko_itm_01_selected, R.drawable.chanko_itm_01_disabled),
    ITM_02(R.drawable.chanko_itm_02_default, R.drawable.chanko_itm_02_selected, R.drawable.chanko_itm_02_disabled),
    ITM_03(R.drawable.chanko_itm_03_default, R.drawable.chanko_itm_03_selected, R.drawable.chanko_itm_03_disabled),
    ITM_04(R.drawable.chanko_itm_04_default, R.drawable.chanko_itm_04_selected, R.drawable.chanko_itm_04_disabled),
    ITM_05(R.drawable.chanko_itm_05_default, R.drawable.chanko_itm_05_selected, R.drawable.chanko_itm_05_disabled),
    ITM_06(R.drawable.chanko_itm_06_default, R.drawable.chanko_itm_06_selected, R.drawable.chanko_itm_06_disabled),
    ITM_07(R.drawable.chanko_itm_07_default, R.drawable.chanko_itm_07_selected, R.drawable.chanko_itm_07_disabled),
    ITM_08(R.drawable.chanko_itm_08_default, R.drawable.chanko_itm_08_selected, R.drawable.chanko_itm_08_disabled),
    ITM_09(R.drawable.chanko_itm_09_default, R.drawable.chanko_itm_09_selected, R.drawable.chanko_itm_09_disabled),
    ITM_10(R.drawable.chanko_itm_10_default, R.drawable.chanko_itm_10_selected, R.drawable.chanko_itm_10_disabled),
    ITM_11(R.drawable.chanko_itm_11_default, R.drawable.chanko_itm_11_selected, R.drawable.chanko_itm_11_disabled),
    ITM_12(R.drawable.chanko_itm_12_default, R.drawable.chanko_itm_12_selected, R.drawable.chanko_itm_12_disabled),
    ITM_13(R.drawable.chanko_itm_13_default, R.drawable.chanko_itm_13_selected, R.drawable.chanko_itm_13_disabled),

    // Budget & Vault & Insights (11 IDs)
    BUD_01(R.drawable.chanko_bud_01_default, R.drawable.chanko_bud_01_selected, R.drawable.chanko_bud_01_disabled),
    BUD_02(R.drawable.chanko_bud_02_default, R.drawable.chanko_bud_02_selected, R.drawable.chanko_bud_02_disabled),
    BUD_03(R.drawable.chanko_bud_03_default, R.drawable.chanko_bud_03_selected, R.drawable.chanko_bud_03_disabled),
    BUD_04(R.drawable.chanko_bud_04_default, R.drawable.chanko_bud_04_selected, R.drawable.chanko_bud_04_disabled),
    BUD_05(R.drawable.chanko_bud_05_default, R.drawable.chanko_bud_05_selected, R.drawable.chanko_bud_05_disabled),
    BUD_06(R.drawable.chanko_bud_06_default, R.drawable.chanko_bud_06_selected, R.drawable.chanko_bud_06_disabled),
    BUD_07(R.drawable.chanko_bud_07_default, R.drawable.chanko_bud_07_selected, R.drawable.chanko_bud_07_disabled),
    BUD_08(R.drawable.chanko_bud_08_default, R.drawable.chanko_bud_08_selected, R.drawable.chanko_bud_08_disabled),
    BUD_09(R.drawable.chanko_bud_09_default, R.drawable.chanko_bud_09_selected, R.drawable.chanko_bud_09_disabled),
    BUD_10(R.drawable.chanko_bud_10_default, R.drawable.chanko_bud_10_selected, R.drawable.chanko_bud_10_disabled),
    BUD_11(R.drawable.chanko_bud_11_default, R.drawable.chanko_bud_11_selected, R.drawable.chanko_bud_11_disabled),

    // System & Status & Chat (18 IDs)
    SYS_01(R.drawable.chanko_sys_01_default, R.drawable.chanko_sys_01_selected, R.drawable.chanko_sys_01_disabled),
    SYS_02(R.drawable.chanko_sys_02_default, R.drawable.chanko_sys_02_selected, R.drawable.chanko_sys_02_disabled),
    SYS_03(R.drawable.chanko_sys_03_default, R.drawable.chanko_sys_03_selected, R.drawable.chanko_sys_03_disabled),
    SYS_04(R.drawable.chanko_sys_04_default, R.drawable.chanko_sys_04_selected, R.drawable.chanko_sys_04_disabled),
    SYS_05(R.drawable.chanko_sys_05_default, R.drawable.chanko_sys_05_selected, R.drawable.chanko_sys_05_disabled),
    SYS_06(R.drawable.chanko_sys_06_default, R.drawable.chanko_sys_06_selected, R.drawable.chanko_sys_06_disabled),
    SYS_07(R.drawable.chanko_sys_07_default, R.drawable.chanko_sys_07_selected, R.drawable.chanko_sys_07_disabled),
    SYS_08(R.drawable.chanko_sys_08_default, R.drawable.chanko_sys_08_selected, R.drawable.chanko_sys_08_disabled),
    SYS_09(R.drawable.chanko_sys_09_default, R.drawable.chanko_sys_09_selected, R.drawable.chanko_sys_09_disabled),
    SYS_10(R.drawable.chanko_sys_10_default, R.drawable.chanko_sys_10_selected, R.drawable.chanko_sys_10_disabled),
    SYS_11(R.drawable.chanko_sys_11_default, R.drawable.chanko_sys_11_selected, R.drawable.chanko_sys_11_disabled),
    SYS_12(R.drawable.chanko_sys_12_default, R.drawable.chanko_sys_12_selected, R.drawable.chanko_sys_12_disabled),
    SYS_13(R.drawable.chanko_sys_13_default, R.drawable.chanko_sys_13_selected, R.drawable.chanko_sys_13_disabled),
    SYS_14(R.drawable.chanko_sys_14_default, R.drawable.chanko_sys_14_selected, R.drawable.chanko_sys_14_disabled),
    SYS_15(R.drawable.chanko_sys_15_default, R.drawable.chanko_sys_15_selected, R.drawable.chanko_sys_15_disabled),
    SYS_16(R.drawable.chanko_sys_16_default, R.drawable.chanko_sys_16_selected, R.drawable.chanko_sys_16_disabled),
    SYS_17(R.drawable.chanko_sys_17_default, R.drawable.chanko_sys_17_selected, R.drawable.chanko_sys_17_disabled),
    SYS_18(R.drawable.chanko_sys_18_default, R.drawable.chanko_sys_18_selected, R.drawable.chanko_sys_18_disabled),
}
