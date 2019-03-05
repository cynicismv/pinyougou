package com.pinyougou.sellergoods.service;

import com.pinyougou.entity.PageResult;
import com.pinyougou.pojo.TbBrand;

import java.util.List;
import java.util.Map;

public interface BrandService {
    public List<TbBrand> findAll();
    public PageResult findPage(int pageNum,int pageSize);
    public void add(TbBrand brand);
    public TbBrand findOne(Long id);
    public void update(TbBrand brand);
    public void delete(Long[] ids);
    public PageResult findByPage(TbBrand brand,int pageNum,int pageSize);
    public List<Map> selectOptionList();
}
