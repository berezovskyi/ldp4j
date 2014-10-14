/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014 Center for Open Middleware.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Artifact    : org.ldp4j.framework:ldp4j-application-data:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-data-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Property;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static com.google.common.base.Preconditions.*;

public final class Validator {

	private static final class ValidationReportImpl implements ValidationReport {
	
		private final List<ValidationLog> logs;
		private final List<ValidationConstraint<?>> unchecked;
		private boolean valid;
		
		private ValidationReportImpl() {
			this.logs=Lists.newArrayList();
			this.unchecked=Lists.newArrayList();
			this.valid=true;
		}
		
		@Override
		public Collection<ValidationFailure> validationFailures() {
			List<ValidationFailure> failures=Lists.newArrayList();
			for(ValidationLog log:this.logs) {
				failures.add(log.validationFailure());
			}
			for(ValidationConstraint<?> constraint:this.unchecked) {
				failures.add(constraint.uncheckedFailure());
			}
			return Collections.unmodifiableList(failures);
		}
	
		private void addValidationLog(ValidationLog log) {
			checkNotNull(log,"Validation log cannot be null");
			this.logs.add(log);
			this.valid=this.valid && log.success();
		}

		private void addUncheckedValidationConstraint(ValidationConstraint<?> constraint) {
			checkNotNull(constraint,"Validation constraint cannot be null");
			this.unchecked.add(constraint);
			this.valid=false;
		}
	
		@Override
		public boolean isValid() {
			return this.valid;
		}
		
	}

	private final Collection<ValidationConstraint<DataSet>> dataSetVC;
	private final Collection<ValidationConstraint<Individual<?,?>>> individualVC;
	private final Collection<ValidationConstraint<Property>> propertyVC;
	
	private final Set<ValidationConstraint<?>> checkedVC;

	private Validator() {
		this.dataSetVC=Lists.newArrayList();
		this.individualVC=Lists.newArrayList();
		this.propertyVC=Lists.newArrayList();
		this.checkedVC=Sets.newIdentityHashSet();
	}
	
	public ValidationReport validate(DataSet dataSet) {
		checkNotNull(dataSet,"Data set cannot be null");
		ValidationReportImpl report=new ValidationReportImpl();
		verifyConstraints(dataSet,this.dataSetVC,report);
		for(Individual<?,?> individual:dataSet) {
			verifyConstraints(individual,this.individualVC,report);
			for(Property property:individual) {
				verifyConstraints(property,this.propertyVC,report);
			}
		}
		verifyCheckedValidationConstraints(report);
		return report;
	}
	
	private void verifyCheckedValidationConstraints(ValidationReportImpl report) {
		verifyCheckedConstraints(this.dataSetVC,report);
		verifyCheckedConstraints(this.individualVC,report);
		verifyCheckedConstraints(this.propertyVC,report);
	}

	private <T> void verifyCheckedConstraints(Collection<ValidationConstraint<T>> constraints, ValidationReportImpl report) {
		for(ValidationConstraint<T> constraint:constraints) {
			if(constraint.mustBeChecked() && !isChecked(constraint)) {
				report.addUncheckedValidationConstraint(constraint);
			}
		}
	}

	private <T> boolean isChecked(ValidationConstraint<T> constraint) {
		return this.checkedVC.contains(constraint);
	}

	private <T> void markChecked(ValidationConstraint<T> constraint) {
		if(constraint.mustBeChecked()) {
			this.checkedVC.add(constraint);
		}
	}

	private <T> void verifyConstraints(T item, Collection<ValidationConstraint<T>> constraints, ValidationReportImpl report) {
		for(ValidationConstraint<T> constraint:constraints) {
			ValidationLog log = constraint.validate(item);
			if(log.checked()) {
				report.addValidationLog(log);
				markChecked(constraint);
			}
		}
	}
	
	private void setDataSetValidationConstraints(Collection<ValidationConstraint<DataSet>> constraints) {
		this.dataSetVC.addAll(constraints);
	}

	private void setIndividualValidationConstraints(Collection<ValidationConstraint<Individual<?,?>>> constraints) {
		this.individualVC.addAll(constraints);
	}
	
	private void setPropertyValidationConstraints(Collection<ValidationConstraint<Property>> constraints) {
		this.propertyVC.addAll(constraints);
	}

	public static ValidatorBuilder builder() {
		return new ValidatorBuilder();
	}
	
	public static final class ValidatorBuilder {
		
		private final Validator helper;
		
		private ValidatorBuilder() {
			this.helper=new Validator();
		}
		
		@SuppressWarnings("unchecked")
		public ValidatorBuilder withDataSetConstraint(ValidationConstraint<DataSet> constraints) {
			this.helper.setDataSetValidationConstraints(Arrays.asList(constraints));
			return this;
		}
		
		@SuppressWarnings("unchecked")
		public ValidatorBuilder withIndividualConstraint(ValidationConstraint<Individual<?,?>> constraints) {
			this.helper.setIndividualValidationConstraints(Arrays.asList(constraints));
			return this;
		}

		@SuppressWarnings("unchecked")
		public ValidatorBuilder withPropertyConstraint(ValidationConstraint<Property> constraints) {
			this.helper.setPropertyValidationConstraints(Arrays.asList(constraints));
			return this;
		}

		public Validator build() {
			return this.helper;
		}
		
	}
	
}
