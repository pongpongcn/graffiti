package com.alltobid.quotabid;

import java.util.EventListener;

public interface BidClientHandlerListener extends EventListener {

	void messageReceived(ReceivedMessage receivedMessage);
}
