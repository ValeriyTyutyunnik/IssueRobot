package IssueRobot;

/* MIT License

Copyright (c) 2019 Tyutyunnik Valeriy

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import java.text.SimpleDateFormat;
import java.util.Date;

// ugly representation of working time 
public class Timetable {
    
    static int robot_period = 0;
    
    public static String Days = "1,2,3,4,5,6,7"; // 1-Monday .. 7-Sunday
    
    // starting and ending hour of the working day (0-23)
    public static int START_DAY = 0;
    public static int END_DAY = 23;
    
    // minutes of starting hours (0-59)
    public static int START_MINUTES = 00;
    // minutes of ending hours (0-59)
    public static int END_MINUTES = 59;

    /**
     * Check is it working time now
     * @return
     */
    public static boolean isWorkingDay() {
        boolean res = false;
        Date date = new Date();
        SimpleDateFormat formatWeekDay = new SimpleDateFormat("u");
        SimpleDateFormat formatHours = new SimpleDateFormat("H");
        SimpleDateFormat formatMinutes = new SimpleDateFormat("m");
        // if it is working day then check hour and minutes
        if (Days.contains(formatWeekDay.format(date))) {
            // если попали в первый или последний час - проверим минуты
            if (START_DAY == Integer.parseInt(formatHours.format(date)) ){
                if (START_MINUTES <= Integer.parseInt(formatMinutes.format(date)) ) {
                    res = true;
                }
            } else if (START_DAY < Integer.parseInt(formatHours.format(date))
                       && Integer.parseInt(formatHours.format(date)) < END_DAY) {
                    res = true;
            } else if (END_DAY == Integer.parseInt(formatHours.format(date)) ){
                if (END_MINUTES > Integer.parseInt(formatMinutes.format(date)) ) {
                    res = true;
                }
            }
        }
        return res;
    }
    
    /**
     * returns nextStartTime (ms)
     * @return 
     */
    public static int getNextStartTime() {
        return robot_period * 1000;
    }
    
}