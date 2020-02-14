package com.anoto.ash.database;

import java.io.InputStream;
import java.util.Properties;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateSessionFactory
{
  private static final SessionFactory sessionFactory;

  public static SessionFactory getSessionFactory()
  {
    return sessionFactory;
  }

  static
  {
    Properties props;
    try
    {
      props = new Properties();
      InputStream is = HibernateSessionFactory.class.getResourceAsStream("/hibernate.properties");
      props.load(is);

      Configuration config = new Configuration();

      config.addInputStream(HibernateSessionFactory.class.getResourceAsStream("/com/anoto/ash/database/mappings/users.hbm.xml"));
      config.addInputStream(HibernateSessionFactory.class.getResourceAsStream("/com/anoto/ash/database/mappings/ash_settings.hbm.xml"));
      config.addInputStream(HibernateSessionFactory.class.getResourceAsStream("/com/anoto/ash/database/mappings/roles.hbm.xml"));
      config.addInputStream(HibernateSessionFactory.class.getResourceAsStream("/com/anoto/ash/database/mappings/formcopies.hbm.xml"));
      config.addInputStream(HibernateSessionFactory.class.getResourceAsStream("/com/anoto/ash/database/mappings/formtypes.hbm.xml"));
      config.addInputStream(HibernateSessionFactory.class.getResourceAsStream("/com/anoto/ash/database/mappings/pads.hbm.xml"));
      config.addInputStream(HibernateSessionFactory.class.getResourceAsStream("/com/anoto/ash/database/mappings/backgrounds.hbm.xml"));
      config.addInputStream(HibernateSessionFactory.class.getResourceAsStream("/com/anoto/ash/database/mappings/log_levels.hbm.xml"));
      config.addInputStream(HibernateSessionFactory.class.getResourceAsStream("/com/anoto/ash/database/mappings/verification_modules.hbm.xml"));
      config.addInputStream(HibernateSessionFactory.class.getResourceAsStream("/com/anoto/ash/database/mappings/export_format.hbm.xml"));
      config.addInputStream(HibernateSessionFactory.class.getResourceAsStream("/com/anoto/ash/database/mappings/thresholds.hbm.xml"));
      config.addInputStream(HibernateSessionFactory.class.getResourceAsStream("/com/anoto/ash/database/mappings/predefined_thresholds.hbm.xml"));
      config.addInputStream(HibernateSessionFactory.class.getResourceAsStream("/com/anoto/ash/database/mappings/fonts.hbm.xml"));
      config.addInputStream(HibernateSessionFactory.class.getResourceAsStream("/com/anoto/ash/database/mappings/dynamic_data_definitions.hbm.xml"));
      config.addInputStream(HibernateSessionFactory.class.getResourceAsStream("/com/anoto/ash/database/mappings/dynamic_data_entries.hbm.xml"));
      config.addInputStream(HibernateSessionFactory.class.getResourceAsStream("/com/anoto/ash/database/mappings/export_method.hbm.xml"));
      config.addInputStream(HibernateSessionFactory.class.getResourceAsStream("/com/anoto/ash/database/mappings/image_format.hbm.xml"));
      config.addInputStream(HibernateSessionFactory.class.getResourceAsStream("/com/anoto/ash/database/mappings/mail_settings.hbm.xml"));
      config.addInputStream(HibernateSessionFactory.class.getResourceAsStream("/com/anoto/ash/database/mappings/formcopies_page_areas.hbm.xml"));
      
      config.addProperties(props);
      sessionFactory = config.buildSessionFactory();
    }
    catch (Throwable ex)
    {
      ex.printStackTrace();
      throw new ExceptionInInitializerError(ex);
    }
  }
}