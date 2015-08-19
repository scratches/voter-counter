/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.Data;

@Entity
@Data
public class Election {
	@Id
	@GeneratedValue
	private Long id;
	private String name;
	private String description;
	private Date completionDate;
	@OneToMany(cascade=CascadeType.PERSIST)
	private List<Candidate> candidates = new ArrayList<Candidate>();

	public Candidate getCandidate(long id) {
		for (Candidate candidate : candidates) {
			if (candidate.getId() == id) {
				return candidate;
			}
		}
		return null;
	}
}