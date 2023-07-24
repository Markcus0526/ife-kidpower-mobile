package org.unicefkidpower.schools.powerband.command;

/**
 * Created by donal_000 on 1/13/2015.
 */
public class BandCommandResponseParser {
	public static final int PARSE_FINISHED = 0;
	public static final int PARSE_WAITING = 1;
	public static final int PARSE_CODE_INCORRECT = 2;
	public static final int PARSE_ERROR = 3;

	protected byte success_code;
	protected byte failure_code;

	public BandCommandResponse _response;

	public byte getSuccessCode() {
		return success_code;
	}

	public byte getFailureCode() {
		return failure_code;
	}

	public int parse(byte code, byte[] response) {
		// checksum
		int length = response.length;
		byte checksum = 0;

		for (int i = 0; i < length; ++i) {
			if (i < length - 1) {
				checksum += response[i];
			}
		}

		if (length < 16 || (0xFF & response[length - 1]) != (checksum & 0xFF)) {
			// error data
			return PARSE_ERROR;
		}
		return PARSE_FINISHED;
	}
}
