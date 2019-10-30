package com.xitu.app.utils;

import java.io.InputStreamReader;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;


public class CsvUtil {
    /**
     * 日志对象
     */
	private static final Logger logger = LoggerFactory.getLogger(CsvUtil.class);

    /**
     * 解析csv文件并转成bean
     * @param file csv文件
     * @param clazz 类
     * @param <T> 泛型
     * @return 泛型bean集合
     */
    public <T> List<T> getCsvData(MultipartFile file, Class<T> clazz) {
        InputStreamReader in;
        try {
            in = new InputStreamReader(file.getInputStream(), "gbk");
            HeaderColumnNameMappingStrategy<T> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(clazz);
            
            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(in)
            		.withSeparator(',')
            		.withQuoteChar('\'')
            		.withMappingStrategy(strategy).build();
            return csvToBean.parse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
