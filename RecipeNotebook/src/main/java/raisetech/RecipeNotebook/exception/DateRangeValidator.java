package raisetech.RecipeNotebook.exception;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import raisetech.RecipeNotebook.domain.RecipeSearchCriteria;

/**
 * レシピ詳細情報の一覧検索時に指定可能な日付の範囲が有効か判断するためのオブジェクトです。
 */
public class DateRangeValidator implements
    ConstraintValidator<DateRangeValidation, RecipeSearchCriteria> {

  private String createDateFromField;
  private String createDateToField;
  private String updateDateFromField;
  private String updateDateToField;

  @Override
  public void initialize(DateRangeValidation constraintAnnotation) {
    this.createDateFromField = constraintAnnotation.createDateFrom();
    this.createDateToField = constraintAnnotation.createDateTo();
    this.updateDateFromField = constraintAnnotation.updateDateFrom();
    this.updateDateToField = constraintAnnotation.updateDateTo();
  }

  @Override
  public boolean isValid(RecipeSearchCriteria criteria, ConstraintValidatorContext context) {
    context.disableDefaultConstraintViolation();
    boolean isValid = true;

    // createdAtのバリデーション
    if (criteria.getCreateDateFrom() != null && criteria.getCreateDateTo() != null) {
      if (criteria.getCreateDateFrom().isAfter(criteria.getCreateDateTo())) {
        context.buildConstraintViolationWithTemplate(
            "終了日が開始日より前の日付になっています"
        ).addPropertyNode("createDateTo").addConstraintViolation();
        isValid = false;
      }
    }

    // updatedAtのバリデーション
    if (criteria.getUpdateDateFrom() != null && criteria.getUpdateDateTo() != null) {
      if (criteria.getUpdateDateFrom().isAfter(criteria.getUpdateDateTo())) {
        context.buildConstraintViolationWithTemplate(
            "終了日が開始日より前の日付になっています"
        ).addPropertyNode("updateDateTo").addConstraintViolation();
        isValid = false;
      }
    }

    return isValid;
  }
}
