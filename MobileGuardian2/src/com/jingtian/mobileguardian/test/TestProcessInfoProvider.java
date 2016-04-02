package com.jingtian.mobileguardian.test;

import java.util.List;

import com.jingtian.mobileguardian.domain.ProcessInfo;
import com.jingtian.mobileguardian.engine.ProcessInfoProvider;

import android.test.AndroidTestCase;
import junit.framework.TestCase;

public class TestProcessInfoProvider extends AndroidTestCase {

	public void testGetProcessInfo() throws Exception{
		List<ProcessInfo> list = ProcessInfoProvider.getProcessesInfo(getContext());
		for (ProcessInfo processInfo : list) {
			System.out.println(list.toString());
		}
	}
}
