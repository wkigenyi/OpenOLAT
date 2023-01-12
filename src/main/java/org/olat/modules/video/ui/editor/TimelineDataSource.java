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
package org.olat.modules.video.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMarker;
import org.olat.modules.video.VideoMarkers;
import org.olat.modules.video.VideoQuestion;
import org.olat.modules.video.VideoQuestions;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.ui.VideoChapterTableRow;
import org.olat.resource.OLATResource;

/**
 * Initial date: 2022-11-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TimelineDataSource implements FlexiTableDataSourceDelegate<TimelineRow> {

	private final OLATResource olatResource;
	private final VideoManager videoManager;

	private List<TimelineRow> rows = new ArrayList<>();

	public TimelineDataSource(OLATResource olatResource) {
		this.olatResource = olatResource;
		videoManager = CoreSpringFactory.getImpl(VideoManager.class);
		loadRows();
	}

	public void loadRows() {
		rows = new ArrayList<>();

		VideoQuestions videoQuestions = videoManager.loadQuestions(olatResource);
		for (VideoQuestion videoQuestion : videoQuestions.getQuestions()) {
			rows.add(new TimelineRow(videoQuestion.getId(), videoQuestion.getBegin().getTime(), 1000,
					TimelineEventType.QUIZ, videoQuestion.getTitle(), videoQuestion.getStyle()));
		}

		VideoSegments videoSegments = videoManager.loadSegments(olatResource);
		for (VideoSegment videoSegment : videoSegments.getSegments()) {
			videoSegments.getCategory(videoSegment.getCategoryId()).ifPresent((c) -> rows.add(
					new TimelineRow(videoSegment.getId(), videoSegment.getBegin().getTime(),
					videoSegment.getDuration() * 1000, TimelineEventType.SEGMENT, c.getLabelAndTitle(),
					c.getColor())
			));
		}

		VideoMarkers videoMarkers = videoManager.loadMarkers(olatResource);
		for (VideoMarker videoMarker : videoMarkers.getMarkers()) {
			rows.add(new TimelineRow(videoMarker.getId(), videoMarker.getBegin().getTime(),
					videoMarker.getDuration() * 1000, TimelineEventType.ANNOTATION, videoMarker.getText(),
					videoMarker.getStyle()));
		}

		List<VideoChapterTableRow> chapters = videoManager.loadChapters(olatResource);
		chapters.forEach((ch) -> {
			String chapterId = generateChapterId(ch);
			long durationInMillis = ch.getEnd().getTime() - ch.getBegin().getTime();
			rows.add(new TimelineRow(chapterId, ch.getBegin().getTime(), durationInMillis,
					TimelineEventType.CHAPTER, ch.getChapterName(), "o_video_marker_gray"));
		});

		rows.sort(Comparator.comparing(TimelineRow::getStartTime));
	}

	private String generateChapterId(VideoChapterTableRow chapter) {
		return "c_" + chapter.getBegin().hashCode() + "_" + chapter.getEnd().hashCode();
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public List<TimelineRow> reload(List<TimelineRow> rows) {
		return Collections.emptyList();
	}

	@Override
	public ResultInfos<TimelineRow> getRows(String query, List<FlexiTableFilter> filters, int firstResult, int maxResults, SortKey... orderBy) {
		List<TimelineRow> resultRows = rows.subList(firstResult, rows.size());
		return new DefaultResultInfos<>(firstResult + resultRows.size(),
				-1, resultRows);
	}

	public enum TimelineFilter {
		TYPE("filter.type"),
		COLOR("filter.color");

		private final String i18nKey;

		TimelineFilter(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		public String getI18nKey() {
			return i18nKey;
		}
	}
}
