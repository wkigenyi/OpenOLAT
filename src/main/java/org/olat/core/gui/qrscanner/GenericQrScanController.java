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
package org.olat.core.gui.qrscanner;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.util.StringHelper;

public class GenericQrScanController extends BasicController {

	private final VelocityContainer mainVC;

	private boolean isScanning = true;

	public GenericQrScanController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("generic_qr_scanner");
		
		String[] js = new String[]{"js/qrscanner/qr-scanner.umd.min.js", "js/qrscanner/qr-scanner-worker.min.js"};
		JSAndCSSComponent qrScannerJS = new JSAndCSSComponent("glossHelpJs", js, null);
		
		mainVC.put("qrScannerJS", qrScannerJS);

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (event.getCommand().equals("QrCodeFoundEvent")) {
			String qrCode = ureq.getHttpReq().getParameter("qrCode");

			if (StringHelper.containsNonWhitespace(qrCode)) {
				fireEvent(ureq, new QrCodeDetectedEvent(qrCode));
			}
		}
	}

	@Override
	protected void doDispose() {
		stopScanner();
        super.doDispose();
	}

	public void startScanner() {
		// TODO Discuss the timeout. Reason: If not there, errors are shown sometimes,
		// because the code is not in DOM yet
		if (!isScanning) {
			JSCommand startScanner = new JSCommand(
					"jQuery(initCameraAndScanner);");
			getWindowControl().getWindowBackOffice().sendCommandTo(startScanner);

			isScanning = true;
		}
	}

	public void stopScanner() {
		// TODO Problem: If the camera is opened once, closed and opened again, it is
		// not closed again properly
		if (isScanning) {
			JSCommand stopScanner = new JSCommand(
					"jQuery(cleanUpScanner);");
			getWindowControl().getWindowBackOffice().sendCommandTo(stopScanner);

			isScanning = false;
		}
	}

}
