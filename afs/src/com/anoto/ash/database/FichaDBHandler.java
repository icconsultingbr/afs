package com.anoto.ash.database;

import com.anoto.ash.AshLogger;
import com.anoto.ash.AshProperties;
import com.anoto.ash.portal.AshSettingsControl;
import com.anoto.ash.portal.ThresholdControl;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class FichaDBHandler extends DBHandler {
	

	public static synchronized ArrayList<?> getAllUsers(String orderSyntax)
			throws HibernateException {
		openSession();
		Session session = getCurrentSession();
		beginTransaction(session);
		List<?> result = session.createQuery(
				"from UserData where userStatus > 0 " + orderSyntax).list();
		AshLogger.logFine("Retrieved all users, number of users returned = "
				+ result.size());
		commitTransaction(session);
		closeSession();

		return new ArrayList<Object>(result);
	}

	
}