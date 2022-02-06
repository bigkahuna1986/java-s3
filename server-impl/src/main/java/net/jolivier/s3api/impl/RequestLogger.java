package net.jolivier.s3api.impl;

import java.util.TimeZone;

import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Slf4jRequestLogWriter;
import org.eclipse.jetty.server.handler.RequestLogHandler;

public class RequestLogger {
	public static final String defaultFormat() {
		return "%{client}a - %u \"%m %U %H\" %s %O \"%{Referer}i\" \"%{User-Agent}i\" %{ms}T";
	}

	public static final String tzFormat(TimeZone tz) {
		return "%{client}a - %u %{dd/MMM/yyyy:HH:mm:ss Z|" + tz.getID()
				+ "}t \"%m %U %H\" %s %O \"%{Referer}i\" \"%{User-Agent}i\" %{ms}T";
	}

	public static void install(Server server, String format) {
		final CustomRequestLog reqLog = new CustomRequestLog(new Slf4jRequestLogWriter(), format);
		final RequestLogHandler requestLogHandler = new RequestLogHandler();
		requestLogHandler.setRequestLog(reqLog);
		final Handler handler = server.getHandler();
		requestLogHandler.setHandler(handler);
		server.addBean(reqLog, true);
		server.setHandler(requestLogHandler);
	}

}
