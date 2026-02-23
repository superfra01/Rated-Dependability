package model;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;

@WebListener
public class DataSourceInitializer implements ServletContextListener {
	
	@Override
	public void contextInitialized(final ServletContextEvent sce) {
		
		final ServletContext context = sce.getServletContext();
	
		DataSource dataSource = null;
		
		try {
			final Context initCtx = new InitialContext();
			final Context envCtx = (Context) initCtx.lookup("java:comp/env");

			dataSource = (DataSource) envCtx.lookup("jdbc/RatedDB");

		} catch (final NamingException e) {
			System.out.println("Error" + e.getMessage());
		}		

		context.setAttribute("DataSource", dataSource);
	}
	
	@Override
	public void contextDestroyed(final ServletContextEvent sce) {
		ServletContextListener.super.contextDestroyed(sce);
	}
}