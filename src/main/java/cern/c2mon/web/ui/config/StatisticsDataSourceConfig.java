package cern.c2mon.web.ui.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@MapperScan(
    value = "cern.c2mon.web.ui.statistics",
    sqlSessionFactoryRef = "statisticsSqlSessionFactory"
)
public class StatisticsDataSourceConfig {

  @Autowired
  private Environment environment;

  @Bean
  @ConfigurationProperties("c2mon.web.statistics.jdbc")
  public DataSourceProperties statisticsDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @ConfigurationProperties("c2mon.web.statistics.jdbc")
  public DataSource statisticsDataSource(DataSourceProperties statisticsDataSourceProperties) {
    return statisticsDataSourceProperties.initializeDataSourceBuilder().build();
  }

  @Bean
  public static SqlSessionFactory statisticsSqlSessionFactory(DataSource statisticsDataSource) throws Exception {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(statisticsDataSource);
    return sessionFactory.getObject();
  }
}
