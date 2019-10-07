package com.allen.imsystem.dao;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

public class C3p0DataSourceFactory extends UnpooledDataSourceFactory {
    public C3p0DataSourceFactory() {
        this.dataSource = new ComboPooledDataSource();
    }
}
