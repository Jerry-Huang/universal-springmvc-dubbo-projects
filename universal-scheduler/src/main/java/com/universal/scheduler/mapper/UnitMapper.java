package com.universal.scheduler.mapper;

import org.apache.ibatis.annotations.Param;

import com.nmtx.support.annotation.DataSource;
import com.universal.scheduler.entity.UnitBean;

@DataSource("mysqlSpuDataSource")
public interface UnitMapper {

    UnitBean findUnitByCode(@Param("code") String code);

}
