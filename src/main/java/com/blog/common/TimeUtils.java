package com.blog.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
	public static String formate(LocalDateTime time) {
		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
		String localTime = df.format(time);
		return localTime;
	}
}
