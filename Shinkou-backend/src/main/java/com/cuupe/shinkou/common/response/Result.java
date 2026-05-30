package com.cuupe.shinkou.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private String code;
    private String message;
    private T data;

    public static <T> Result<T> success(String code, String message, T data){
        return new Result<>(code, message, data);
    }
    public static <T> Result<T> success(T data){
        return new Result<>("SUCCESS", "success", data);
    }
    public static Result<Void> success(){
        return new Result<>("SUCCESS","success", null);
    }
    public static <T> Result<T> fail(String code, String message){
        return new Result<>(code, message, null);
    }
}
