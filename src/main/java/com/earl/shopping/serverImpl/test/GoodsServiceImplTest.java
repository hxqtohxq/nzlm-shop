package com.earl.shopping.serverImpl.test;

import static org.junit.Assert.fail;


import org.junit.Test;

import com.earl.shopping.server.GoodsService;
import com.earl.shopping.serverImpl.GoodsServiceImpl;
import com.earl.solrj.query.pojo.GoodsPo;



public class GoodsServiceImplTest {
	
	GoodsService goodsService  = new GoodsServiceImpl();

	@Test
	public void testGoodsServiceImpl() {
		fail("Not yet implemented");
	}

	@Test
	public void testSaveGoods() {
		GoodsPo goods = new GoodsPo();
		goodsService.save(goods);
	}

	@Test
	public void testQueryByWord() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteById(){
		
		goodsService.deleteById(1);
		
	}
	
}
