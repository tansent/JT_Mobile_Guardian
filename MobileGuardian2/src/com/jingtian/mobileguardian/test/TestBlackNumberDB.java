package com.jingtian.mobileguardian.test;

import java.util.List;
import java.util.Random;

import com.jingtian.mobileguardian.db.BlackNumberDBOpenHelper;
import com.jingtian.mobileguardian.db.dao.BlackNumberDAO;
import com.jingtian.mobileguardian.domain.BlackNumberInfo;

import android.test.AndroidTestCase;

public class TestBlackNumberDB extends AndroidTestCase {

	public void testCreateDB() throws Exception{
		BlackNumberDBOpenHelper helper = new BlackNumberDBOpenHelper(getContext());
		helper.getWritableDatabase();
	}
	
	public void testAdd() throws Exception{
		BlackNumberDAO dao = new BlackNumberDAO(getContext());
		long number = 1350000000;
		Random random = new Random();
		for (int i = 0; i < 100; i++) {
			dao.addBlackNumber(String.valueOf(i + number), String.valueOf(random.nextInt(3)+1));
		}
	}
	
	public void testDelete() throws Exception{
		BlackNumberDAO dao = new BlackNumberDAO(getContext());
		dao.deleteBlackNumber("110");
	}
	
	public void testDeleteAll() throws Exception{
		BlackNumberDAO dao = new BlackNumberDAO(getContext());
		dao.deleteAll();
	}
	
	public void testUpdate() throws Exception{
		BlackNumberDAO dao = new BlackNumberDAO(getContext());
		dao.updateBlackNumberMode("110", "2");
	}
	
	public void testSearch() throws Exception{
		BlackNumberDAO dao = new BlackNumberDAO(getContext());
		boolean result = dao.searchBlackNumber("110");
		assertEquals(true, result);  //green bar: true  |  red bar: false
	}
	
	public void testSearchAll() throws Exception{
		BlackNumberDAO dao = new BlackNumberDAO(getContext());
		List<BlackNumberInfo> result = dao.searchAllBlackNumber();
		for(BlackNumberInfo number : result){
			System.out.println(number.toString());
		}
	}
}
