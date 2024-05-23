/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggercodegen.annotation.projection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Projection {}
