package database;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateDao {
    private final DatabaseManager dbManager;

    public DateDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void setDate(String inputDate) throws Exception {
        validateDate(inputDate);
        List<String> lines = dbManager.readDateFile();

        lines.set(0, inputDate);
        dbManager.writeDateFile(lines);
    }

    public String getDate() throws IOException {
        List<String> lines = dbManager.readDateFile();
        return lines.get(0);
    }

    public void validateDate(String inputDate) throws Exception {
        if (inputDate.length() != 8) {
            throw new DateTimeParseException("입력된 날짜는 8자리여야 합니다.", inputDate, 0);
        }

        try {
            LocalDate.parse(inputDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (DateTimeParseException e) {
            throw new DateTimeParseException("유효하지 않은 날짜 형식입니다.", inputDate, 0);
        }
    }

    public int parseYear(String date) {
        return Integer.parseInt(date.substring(0, 4));
    }

    public int parseMonth(String date) {
        return Integer.parseInt(date.substring(4, 6));
    }

    public int parseDay(String date) {
        return Integer.parseInt(date.substring(6, 8));
    }

    public int calculateMonth(String pastDate, String presentDate) {
        int pastYear = parseYear(pastDate);
        int pastMonth = parseMonth(pastDate);

        int presentYear = parseYear(presentDate);
        int presentMonth = parseMonth(presentDate);

        // 연도와 월을 이용하여 개월 수 계산
        int yearDifference = presentYear - pastYear;
        return yearDifference * 12 + (presentMonth - pastMonth);
    }

 // 날짜 차이 계산 메서드
    public long calculateDaysBetween(String date1, String date2) throws DateTimeParseException {
        // 두 날짜를 파싱
        LocalDate localDate1 = LocalDate.parse(date1, DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate localDate2 = LocalDate.parse(date2, DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 두 날짜가 연도를 넘어갈 경우를 고려
        if (localDate1.isAfter(localDate2)) {
            localDate2 = localDate2.plusYears(1);
        }

        return java.time.temporal.ChronoUnit.DAYS.between(localDate1, localDate2);
    }

//    // 날짜 차이 계산 메서드
//    public long calculateDaysBetween(String date1, String date2) throws DateTimeParseException {
//        // 현재 연도를 기준으로 두 날짜를 파싱
//        LocalDate localDate1 = parseDateWithCurrentYear(date1);
//        LocalDate localDate2 = parseDateWithCurrentYear(date2);
//
//        // 두 날짜가 연도를 넘어갈 경우를 고려
//        if (localDate1.isAfter(localDate2)) {
//            localDate2 = localDate2.plusYears(1);
//        }
//
//        return java.time.temporal.ChronoUnit.DAYS.between(localDate1, localDate2);
//    }
//
//    // 날짜 차이 계산을 위해 임시로 yyyyMMdd date 생성 메서드
//    private LocalDate parseDateWithCurrentYear(String date) {
//        int currentYear = LocalDate.now().getYear();
//        return LocalDate.parse(currentYear + date, DateTimeFormatter.ofPattern("yyyyMMdd"));
//    }


//    // 마지막으로 업데이트된 날짜와 오늘이 같은 달인지 확인하는 메서드(맞으면 true를, 아니면 false return)
//    public boolean isSameMonth(String accountNumber) throws IOException {
//        // 가장 마지막에 업데이트된 날짜를 가져옵니다.
//        String lastUpdatedDate = getLastUpdatedDate(accountNumber);
//        // 오늘 날짜를 가져옵니다.
//        String today = getDate();
////        System.out.println("lastUpdatedDate : " + lastUpdatedDate);
////        System.out.println("today : " + today);
//
//        // 두 날짜가 같은 달인지 확인합니다.
//        return getMonthFromDateString(lastUpdatedDate) == getMonthFromDateString(today);
//    }
    
 // 마지막으로 업데이트된 날짜와 오늘이 같은 년도와 달인지 확인하는 메서드(맞으면 true를, 아니면 false return)
    public boolean isSameYearAndMonth(String accountNumber) throws IOException {
        // 가장 마지막에 업데이트된 날짜를 가져옵니다.
        String lastUpdatedDate = getLastUpdatedDate(accountNumber);
        // 오늘 날짜를 가져옵니다.
        String today = getDate();

        // 두 날짜가 같은 년도와 달인지 확인합니다.
        return getYearAndMonthFromDateString(lastUpdatedDate).equals(getYearAndMonthFromDateString(today));
    }
    
 // 마지막으로 업데이트된 날짜와 파싱된 날짜가 같은 년도와 달인지 확인하는 메서드(맞으면 true를, 아니면 false return)
    public boolean isSameYearAndMonthInLine(String accountNumber, String parsedDate) throws IOException {
        // 가장 마지막에 업데이트된 날짜를 가져옵니다.
        String lastUpdatedDate = getLastUpdatedDate(accountNumber);

        // 두 날짜가 같은 년도와 달인지 확인합니다.
        return getYearAndMonthFromDateString(lastUpdatedDate).equals(getYearAndMonthFromDateString(parsedDate));
    }


    // 날짜 문자열에서 년도와 월을 추출하는 메서드
    private String getYearAndMonthFromDateString(String date) {
        return date.substring(0, 6);  // 년도와 월을 반환합니다.
    }


 // account/계좌번호.txt에서 마지막으로 업데이트 된 날짜를 가져오는 메서드
    public String getLastUpdatedDate(String accountNumber) throws IOException {
        List<String> lines = dbManager.readAccountFile(accountNumber);
        Pattern pattern = Pattern.compile("(\\d+년 \\d+월 \\d+일)");
        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = lines.get(i);
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                // 년, 월, 일을 추출합니다.
                String year = matcher.group(1).split("년")[0].trim();
                String month = matcher.group(1).split("년")[1].split("월")[0].trim();
                String day = matcher.group(1).split("월")[1].replace("일", "").trim();
                
                // 월과 일이 한 자리수인 경우 앞에 0을 추가합니다.
                if (month.length() == 1) {
                    month = "0" + month;
                }
                if (day.length() == 1) {
                    day = "0" + day;
                }
                
                return year + month + day;
            }
        }
        return null;  // 날짜를 찾지 못했을 때 null을 반환합니다.
    }




    // account/계좌번호.txt에서 몇 월인지 가져오는 메서드
//    private int getMonthFromAccountFileDate(String date) {
//        return Integer.parseInt(date.split(" ")[0]);
//    }

//    // getDate에서 month 정보만 가져오는 메서드
//    private int getMonthFromDateString(String date) {
//        return Integer.parseInt(date.substring(4, 6));
//    }


}