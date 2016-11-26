package com.ningkangyuan.kpi;

/**
 *
 * Created by xuchun on 2016/8/23.
 */
public class Measure {

    public static class XueYa {
        /**
         * 血压
         */
        public static final String INSPECT_CODE = "C01";
        /**
         * 收缩压
         */
        public static final String CODE_SYS = "SYS";
        /**
         * 舒张压
         */
        public static final String CODE_DIA = "DIA";
        /**
         * 脉率
         */
        public static final String CODE_PR = "PR";
    }

    public static class XueTang {
        /**
         * 血糖
         */
        public static final String INSPECT_CODE = "C02";
        /**
         * 随机血糖
         */
        public static final String CODE_GLU0 = "GLU0";
        /**
         * 餐前血糖
         */
        public static final String CODE_GLU1 = "GLU1";
        /**
         * 餐后血糖
         */
        public static final String CODE_GLU2 = "GLU2";
    }

    public static class XingTi {
        /**
         * 形体
         */
        public static final String INSPECT_CODE = "C03";
        /**
         * 身高
         */
        public static final String HEIGHT = "HEIGHT";
        /**
         * 体重
         */
        public static final String WEIGHT = "WEIGHT";
        /**
         * BMI
         */
        public static final String BMI = "BMI";
    }

    public static class TiWen {
        /**
         * 体温
         */
        public static final String INSPECT_CODE = "C04";
        /**
         * 体温
         */
        public static final String TEMP = "TEMP";
    }

    public static class XueYe {
        /**
         * 血液
         */
        public static final String INSPECT_CODE = "C06";
        /**
         * 白细胞
         */
        public static final String LEU = "LEU";
        /**
         * 亚硝酸盐
         */
        public static final String NIT = "NIT";
        /**
         * 尿胆原
         */
        public static final String UBG = "UBG";
    }
}