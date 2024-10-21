package raisetech.RecipeNotebook.exception;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * レシピ詳細情報の一覧検索時に指定可能な日付の範囲に関するバリデーションを行うカスタムアノテーションです。
 */
@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DateRangeValidation {

  String message() default "日付の範囲指定が不正です";

  String createDateFrom() default "";

  String createDateTo() default "";

  String updateDateFrom() default "";

  String updateDateTo() default "";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
