package com.ningkangyuan.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xuchun on 2016/8/22.
 */
public class VerifyUtil {

    public static String IDCardValidate(String IDStr) {
        String tipInfo = "";// 记录错误信息
        String Ai = "";
        // 判断号码的长度 15位或18位
        if (IDStr.length() != 15 && IDStr.length() != 18) {
            tipInfo = "ID number length should be15Or18position。";
            return tipInfo;
        }


        // 18位身份证前17位位数字，如果是15位的身份证则所有号码都为数字
        if (IDStr.length() == 18) {
            Ai = IDStr.substring(0, 17);
        } else if (IDStr.length() == 15) {
            Ai = IDStr.substring(0, 6) + "19" + IDStr.substring(6, 15);
        }
        if (isNumeric(Ai) == false) {
            tipInfo = "ID15Bit numbers should be numeric ; 18Bit number in addition to the last one，All should be numbers。";
            return tipInfo;
        }


        // 判断出生年月是否有效
        String strYear = Ai.substring(6, 10);// 年份
        String strMonth = Ai.substring(10, 12);// 月份
        String strDay = Ai.substring(12, 14);// Date,
        if (isDate(strYear + "-" + strMonth + "-" + strDay) == false) {
            tipInfo = "Birth date is not valid。";
            return tipInfo;
        }
        GregorianCalendar gc = new GregorianCalendar();
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if ((gc.get(Calendar.YEAR) - Integer.parseInt(strYear)) > 150
                    || (gc.getTime().getTime() - s.parse(
                    strYear + "-" + strMonth + "-" + strDay).getTime()) < 0) {
                tipInfo = "ID card birthday is not valid。";
                return tipInfo;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        if (Integer.parseInt(strMonth) > 12 || Integer.parseInt(strMonth) == 0) {
            tipInfo = "ID card month is invalid";
            return tipInfo;
        }
        if (Integer.parseInt(strDay) > 31 || Integer.parseInt(strDay) == 0) {
            tipInfo = "Id date is invalid";
            return tipInfo;
        }


        // 判断地区码是否有效
        Hashtable areacode = GetAreaCode();
        //如果身份证前两位的地区码不在Hashtable，则地区码有误
        if (areacode.get(Ai.substring(0, 2)) == null) {
            tipInfo = "ID area code error。";
            return tipInfo;
        }

        if(isVarifyCode(Ai,IDStr)==false){
            tipInfo = "ID check code is invalid，Not a valid ID number.";
            return tipInfo;
        }


        return tipInfo;
    }


    /*
     * 判断第18位校验码是否正确
    * 第18位校验码的计算方式：
       　　1. 对前17位数字本体码加权求和
       　　公式为：S = Sum(Ai * Wi), i = 0, ... , 16
       　　其中Ai表示第i个位置上的身份证号码数字值，Wi表示第i位置上的加权因子，其各位对应的值依次为： 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2
       　　2. 用11对计算结果取模
       　　Y = mod(S, 11)
       　　3. 根据模的值得到对应的校验码
       　　对应关系为：
       　　 Y值：     0  1  2  3  4  5  6  7  8  9  10
       　　校验码： 1  0  X  9  8  7  6  5  4  3   2
    */
    private static boolean isVarifyCode(String Ai,String IDStr) {
        String[] VarifyCode = { "1", "0", "X", "9", "8", "7", "6", "5", "4","3", "2" };
        String[] Wi = { "7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7","9", "10", "5", "8", "4", "2" };
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum = sum + Integer.parseInt(String.valueOf(Ai.charAt(i))) * Integer.parseInt(Wi[i]);
        }
        int modValue = sum % 11;
        String strVerifyCode = VarifyCode[modValue];
        Ai = Ai + strVerifyCode;
        if (IDStr.length() == 18) {
            if (Ai.equals(IDStr) == false) {
                return false;

            }
        }
        return true;
    }


    /**
     * 将所有地址编码保存在一个Hashtable中
     * @return Hashtable 对象
     */

    private static Hashtable GetAreaCode() {
        Hashtable hashtable = new Hashtable();
        hashtable.put("11", "beijing");
        hashtable.put("12", "tianjin");
        hashtable.put("13", "hebei");
        hashtable.put("14", "shanxi");
        hashtable.put("15", "innermongolia");
        hashtable.put("21", "liaoning");
        hashtable.put("22", "jilin");
        hashtable.put("23", "heilongjiang");
        hashtable.put("31", "shanghai");
        hashtable.put("32", "jiangsu");
        hashtable.put("33", "zhejiang");
        hashtable.put("34", "anhui");
        hashtable.put("35", "fujian");
        hashtable.put("36", "jiangxi");
        hashtable.put("37", "shandong");
        hashtable.put("41", "henan");
        hashtable.put("42", "hubei");
        hashtable.put("43", "hunan");
        hashtable.put("44", "guangdong");
        hashtable.put("45", "guangxi");
        hashtable.put("46", "hainan");
        hashtable.put("50", "chongqing");
        hashtable.put("51", "sichuan");
        hashtable.put("52", "guizhou");
        hashtable.put("53", "yunnan");
        hashtable.put("54", "tibet");
        hashtable.put("61", "shaanxi");
        hashtable.put("62", "gansu");
        hashtable.put("63", "qinghai");
        hashtable.put("64", "ningxia");
        hashtable.put("65", "xinjiang");
        hashtable.put("71", "taiwan");
        hashtable.put("81", "hongkong");
        hashtable.put("82", "macau");
        hashtable.put("91", "abroad");
        return hashtable;
    }

    /**
     * 判断字符串是否为数字,0-9重复0次或者多次
     * @param strnum
     * @return
     */
    private static boolean isNumeric(String strnum) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(strnum);
        if (isNum.matches()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 功能：判断字符串出生日期是否符合正则表达式：包括年月日，闰年、平年和每月31天、30天和闰月的28天或者29天
     *
     * @param strDate
     * @return
     */
    private static boolean isDate(String strDate) {
        Pattern pattern = Pattern.compile("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))?$");
        Matcher m = pattern.matcher(strDate);
        if (m.matches()) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 手机号验证
     *
     * @param  str
     * @return 验证通过返回true
     */
    public static boolean isMobile(String str) {
        Pattern p = null;
        Matcher m = null;
        boolean b = false;
        p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$"); // 验证手机号
        m = p.matcher(str);
        b = m.matches();
        return b;
    }
}