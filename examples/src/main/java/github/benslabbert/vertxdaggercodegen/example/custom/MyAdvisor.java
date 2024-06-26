/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggercodegen.example.custom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface MyAdvisor {

  String ADVISOR = "github.benslabbert.vertxdaggercodegen.example.custom.MyAdvice";
}
