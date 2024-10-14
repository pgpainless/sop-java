// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite

import java.lang.annotation.Inherited

@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class AbortOnUnsupportedOption
