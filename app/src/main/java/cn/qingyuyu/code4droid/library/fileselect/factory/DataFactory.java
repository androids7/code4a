package cn.qingyuyu.code4droid.library.fileselect.factory;

import android.content.Context;
import android.os.Bundle;

import cn.qingyuyu.code4droid.library.fileselect.model.DataModel;
import cn.qingyuyu.code4droid.library.fileselect.model.FileDataModel;


/**
 * Created by admin on 2016/11/22.
 */
public class DataFactory {

    private static DataFactory dataFactory;
    private FileDataModel fileDataModel;

    private DataFactory() {
    }

    public static DataFactory getInstance() {
        if (dataFactory == null) {
            dataFactory = new DataFactory();
        }
        return dataFactory;
    }

    public DataModel createDataModel(Context context, DataType dataType, Bundle args) {

        DataModel dataModel = null;

        switch (dataType) {

            case fileData:
                dataModel = createFileDataModel(context, args);
                break;

        }

        assert dataModel != null;
        dataModel.setArgs(args);
        return dataModel;
    }

    private FileDataModel createFileDataModel(Context context, Bundle args) {
        if (fileDataModel == null) {
            fileDataModel = new FileDataModel(context, args);
        }
        return fileDataModel;
    }

}
