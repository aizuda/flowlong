package com.flowlong.bpm.engine.access.dialect;

import com.flowlong.bpm.engine.access.Page;

/**
 * db2方言
 *
 * @author hubin
 * @since 1.0
 */
public class Db2Dialect implements Dialect {

    @Override
    public String getPageSql(String sql, Page<?> page) {
        StringBuffer pageSql = new StringBuffer(sql.length() + 100);
        pageSql.append("SELECT * FROM  ( SELECT B.*, ROWNUMBER() OVER() AS RN FROM ( ");
        pageSql.append(sql);
        long start = (page.getPageNo() - 1) * page.getPageSize() + 1;
        pageSql.append(" ) AS B )AS A WHERE A.RN BETWEEN ");
        pageSql.append(start);
        pageSql.append(" AND ");
        pageSql.append(start + page.getPageSize());
        return pageSql.toString();
    }
}
