package com.esproc.jdbc;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

import com.scudata.app.config.RaqsoftConfig;
import com.scudata.common.Logger;
import com.scudata.common.StringUtils;

/**
 * esProc jdbc驱动类，实现了java.sql.Driver。 
 * URL参数如下: 
 * username=UserName用户名。
 * config=raqsoftConfig.xml指定配置文件名称。配置文件只会加载一次。
 * onlyserver=true/false。true在服务器执行，false先在本地执行，找不到时在配置的服务器上执行。
 * debugmode=true/false。true会输出调试信息，false不输出调试信息
 * 
 */
public class InternalDriver implements java.sql.Driver, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public InternalDriver() {
		JDBCUtil.log("InternalDriver-1");
	}

	/**
	 * Statically register driver class
	 */
	static {
		try {
			DriverManager.registerDriver(new com.esproc.jdbc.InternalDriver());
		} catch (SQLException e) {
			throw new RuntimeException(JDBCMessage.get().getMessage(
					"error.cantregist"), e);
		}
	}

	/**
	 * Attempts to make a database connection to the given URL. The driver should
	 * return "null" if it realizes it is the wrong kind of driver to connect to the
	 * given URL. This will be common, as when the JDBC driver manager is asked to
	 * connect to a given URL it passes the URL to each loaded driver in turn.
	 * 
	 * @param url  the URL of the database to which to connect
	 * @param info a list of arbitrary string tag/value pairs as connection
	 *             arguments.
	 */
	public Connection connect(String url, Properties info) throws SQLException {
		JDBCUtil.log("InternalDriver-2");
		return connect(url, info, null);
	}

	/**
	 * Extended connection function. Added RaqsoftConfig as a parameter
	 * 
	 * @param url  the URL of the database to which to connect
	 * @param info a list of arbitrary string tag/value pairs as connection
	 *             arguments.
	 * @param rc   The RaqsoftConfig object
	 * @return Connection
	 * @throws SQLException
	 */
	public Connection connect(String url, Properties info, RaqsoftConfig rc)
			throws SQLException {
		JDBCUtil.log("InternalDriver-3");
		if (!acceptsURL(url)) {
			// The URL format is incorrect. Expected: {0}.
			throw new SQLException(JDBCMessage.get().getMessage(
					"jdbcdriver.incorrecturl", DEMO_URL));
		}
		String config = info.getProperty(KEY_CONFIG);
		String sonlyServer = info.getProperty(KEY_ONLY_SERVER);
		String sdebugmode = info.getProperty("debugmode");
		String[] parts = url.split("&");
		for (int i = 0; i < parts.length; i++) {
			int i3 = parts[i].toLowerCase().indexOf(
					KEY_CONFIG.toLowerCase() + "=");
			int i4 = parts[i].toLowerCase().indexOf(
					KEY_ONLY_SERVER.toLowerCase() + "=");
			int i6 = parts[i].toLowerCase().indexOf(
					KEY_DEBUGMODE.toLowerCase() + "=");
			if (i3 >= 0)
				config = parts[i].substring(i3 + 7);
			if (i4 >= 0)
				sonlyServer = parts[i].substring(i4 + 11);
			if (i6 >= 0)
				sdebugmode = parts[i].substring(i6 + 10);
		}
		boolean isOnlyServer = false;
		if (StringUtils.isValidString(sonlyServer))
			try {
				isOnlyServer = Boolean.valueOf(sonlyServer);
			} catch (Exception e) {
				Logger.warn("Invalid onlyServer parameter: " + sonlyServer);
			}
		JDBCUtil.log(KEY_ONLY_SERVER + "=" + isOnlyServer);
		boolean isDebugMode = false;
		if (StringUtils.isValidString(sdebugmode)) {
			try {
				isDebugMode = Boolean.valueOf(sdebugmode);
			} catch (Exception e) {
			}
		}
		JDBCUtil.isDebugMode = isDebugMode;
		Server server = Server.getInstance();
		server.initConfig(rc, config);
		InternalConnection con = server.connect(this);
		if (con != null) {
			con.setUrl(url);
			con.setClientInfo(info);
			con.setOnlyServer(isOnlyServer);
		}
		return con;
	}

	/**
	 * Retrieves whether the driver thinks that it can open a connection to the
	 * given URL. Typically drivers will return true if they understand the
	 * sub-protocol specified in the URL and false if they do not.
	 * 
	 * @param url the URL of the database
	 * @return true if this driver understands the given URL; false otherwise
	 */
	public boolean acceptsURL(String url) throws SQLException {
		JDBCUtil.log("InternalDriver-4");
		if (url == null) {
			return false;
		}
		return url.toLowerCase().startsWith(ACCEPT_URL);
	}

	/**
	 * 可接受的URL
	 */
	private static final String ACCEPT_URL = "jdbc:esproc:local:";

	/**
	 * 示例
	 */
	private static final String DEMO_URL = "jdbc:esproc:local://";

	/**
	 * Gets information about the possible properties for this driver.
	 * 
	 * @param url  the URL of the database to which to connect
	 * @param info a proposed list of tag/value pairs that will be sent on connect
	 *             open
	 * @return an array of DriverPropertyInfo objects describing possible
	 *         properties. This array may be an empty array if no properties are
	 *         required.
	 */
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			throws SQLException {
		JDBCUtil.log("InternalDriver-5");
		DriverPropertyInfo[] dpis = new DriverPropertyInfo[2];
		dpis[0] = new DriverPropertyInfo(KEY_CONFIG, null);
		dpis[1] = new DriverPropertyInfo(KEY_ONLY_SERVER, "false");
		return dpis;
	}

	/**
	 * Retrieves the driver's major version number. Initially this should be 1.
	 * 
	 * @return this driver's major version number
	 */
	public int getMajorVersion() {
		JDBCUtil.log("InternalDriver-6");
		return 1;
	}

	/**
	 * Gets the driver's minor version number. Initially this should be 0.
	 * 
	 * @return this driver's minor version number
	 */
	public int getMinorVersion() {
		JDBCUtil.log("InternalDriver-7");
		return 0;
	}

	/**
	 * Reports whether this driver is a genuine JDBC Compliant driver. A driver may
	 * only report true here if it passes the JDBC compliance tests; otherwise it is
	 * required to return false.
	 * 
	 * @return true if this driver is JDBC Compliant; false otherwise
	 */
	public boolean jdbcCompliant() {
		JDBCUtil.log("InternalDriver-8");
		return true;
	}

	/**
	 * Return the parent Logger of all the Loggers used by this driver. This should
	 * be the Logger farthest from the root Logger that is still an ancestor of all
	 * of the Loggers used by this driver. Configuring this Logger will affect all
	 * of the log messages generated by the driver. In the worst case, this may be
	 * the root Logger.
	 * 
	 * 
	 * @return the parent Logger for this driver
	 */
	public java.util.logging.Logger getParentLogger()
			throws SQLFeatureNotSupportedException {
		JDBCUtil.log("InternalDriver-9");
		Logger.debug(JDBCMessage.get().getMessage("error.methodnotimpl",
				"getParentLogger()"));
		return null;
	}

	private static final String KEY_CONFIG = "config";
	private static final String KEY_ONLY_SERVER = "onlyServer";

	// 仅调试用
	private static final String KEY_DEBUGMODE = "debugmode";
}
