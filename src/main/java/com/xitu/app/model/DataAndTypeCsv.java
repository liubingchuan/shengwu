package com.xitu.app.model;

import com.opencsv.bean.CsvBindByName;

public class DataAndTypeCsv {

	@CsvBindByName(column="Title")
	private String title;
	
}
