package com.wuzuhao.cpm.user.exception;

import com.wuzuhao.cpm.common.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            message = "系统运行时异常：" + e.getClass().getSimpleName();
        }
        return Result.error(message);
    }

    /**
     * 处理参数校验异常（@RequestBody）
     */
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    public Result<?> handleBindException(Exception e) {
        StringBuilder message = new StringBuilder();
        if (e instanceof BindException) {
            BindException be = (BindException) e;
            for (FieldError error : be.getBindingResult().getFieldErrors()) {
                message.append(error.getDefaultMessage()).append("; ");
            }
        } else if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException me = (MethodArgumentNotValidException) e;
            for (FieldError error : me.getBindingResult().getFieldErrors()) {
                message.append(error.getDefaultMessage()).append("; ");
            }
        }
        return Result.error(message.toString());
    }

    /**
     * 处理Content-Type不支持异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result<?> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        return Result.error(400, "请求格式错误：请使用 Content-Type: application/json 发送JSON格式数据");
    }

    /**
     * 处理HTTP方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return Result.error(405, "请求方法不支持：请使用 " + String.join(", ", e.getSupportedMethods()) + " 方法");
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return Result.error(400, "缺少必需参数：" + e.getParameterName());
    }

    /**
     * 处理参数校验异常（@RequestParam）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        StringBuilder message = new StringBuilder();
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            message.append(violation.getMessage()).append("; ");
        }
        return Result.error(message.toString());
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            message = "系统异常：" + e.getClass().getSimpleName();
        } else {
            message = "系统异常：" + message;
        }
        return Result.error(message);
    }
}

