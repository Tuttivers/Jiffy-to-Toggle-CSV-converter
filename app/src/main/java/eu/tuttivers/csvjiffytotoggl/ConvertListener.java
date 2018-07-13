package eu.tuttivers.csvjiffytotoggl;

import android.content.Context;

public interface ConvertListener {

    void succesfullyConverted(String convertedFilePath);

    void onErrorConverted(String errorMessage);

    Context getContext();
}
