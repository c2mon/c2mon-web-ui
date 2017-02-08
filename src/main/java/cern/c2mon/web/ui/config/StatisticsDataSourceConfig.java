package cern.c2mon.web.ui.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@MapperScan(
    value = "cern.c2mon.web.ui.legacy.statistics",
    sqlSessionFactoryRef = "statisticsSqlSessionFactory"
)
public class StatisticsDataSourceConfig {

  @Autowired
  private Environment environment;

  @Bean
  @Primary
  public DataSource statisticsDataSource() {
    return DataSourceBuilder.create()
        .url(environment.getProperty("c2mon.web.statistics.jdbc.url"))
        .username(environment.getProperty("c2mon.web.statistics.jdbc.username"))
        .password(environment.getProperty("c2mon.web.statistics.jdbc.password")).build();
  }

  @Bean
  @Primary
  public static SqlSessionFactory statisticsSqlSessionFactory(DataSource statisticsDataSource) throws Exception {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(statisticsDataSource);
    return sessionFactory.getObject();
  }
}
