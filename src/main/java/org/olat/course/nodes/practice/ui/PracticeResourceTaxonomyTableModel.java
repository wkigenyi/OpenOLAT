/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.practice.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 6 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeResourceTaxonomyTableModel extends DefaultFlexiTableDataModel<PracticeResourceTaxonomyRow>
implements SortableFlexiTableDataModel<PracticeResourceTaxonomyRow> {
	
	private static final PracticeTaxonomyCols[] COLS = PracticeTaxonomyCols.values();
	
	private final Locale locale;
	
	public PracticeResourceTaxonomyTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<PracticeResourceTaxonomyRow> rows = new PracticeResourceTaxonomyTableSortDelegate(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		PracticeResourceTaxonomyRow rRow = getObject(row);
		return getValueAt(rRow, col);
	}

	@Override
	public Object getValueAt(PracticeResourceTaxonomyRow row, int col) {
		switch(COLS[col]) {
			case taxonomyLevel: return row;
			case numOfQuestions: return row.getNumOfQuestions();
			default: return "ERROR";
		}
	}
	
	public enum PracticeTaxonomyCols implements FlexiSortableColumnDef {
		taxonomyLevel("table.header.taxonomy.level"),
		numOfQuestions("table.header.questions");
		
		private final String i18nKey;
		
		private PracticeTaxonomyCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}

}
