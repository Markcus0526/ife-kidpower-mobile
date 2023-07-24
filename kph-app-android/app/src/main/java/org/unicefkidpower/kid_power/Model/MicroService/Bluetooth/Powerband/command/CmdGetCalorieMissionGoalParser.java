package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

/**
 * Created by Dayong Li on 9/29/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdGetCalorieMissionGoalParser extends BandCommandParser {
	private static CmdGetCalorieMissionGoalParser _instance;

	private CmdGetCalorieMissionGoalParser() {
		success_code = BandCommandResponse.CODE_GET_CALORIEMISSIONGOAL_SUCCESS;
		failed_code = BandCommandResponse.CODE_GET_CALORIEMISSIONGOAL_FAILURE;
	}

	public static CmdGetCalorieMissionGoalParser sharedInstance() {
		if (_instance == null)
			_instance = new CmdGetCalorieMissionGoalParser();
		return _instance;
	}

	@Override
	public int parse(byte code, byte[] response) {
		_response = new CmdGetCalorieMissionGoalRes();

		int ret = super.parse(code, response);
		if (ret != PARSE_FINISHED)
			return ret;

		if (code == success_code) {
			byte enableGoal = response[1];

			int pos = 2;
			int byte1 = Math.abs(response[pos + 0] & 0xFF);
			int byte2 = Math.abs(response[pos + 1] & 0xFF);
			int byte3 = Math.abs(response[pos + 2] & 0xFF);
			int byte4 = Math.abs(response[pos + 3] & 0xFF);
			long goal = (byte1 << 24) + (byte2 << 16) + (byte3 << 8) + byte4;

			pos = 6;
			byte1 = Math.abs(response[pos + 0] & 0xFF);
			byte2 = Math.abs(response[pos + 1] & 0xFF);
			byte3 = Math.abs(response[pos + 2] & 0xFF);
			byte4 = Math.abs(response[pos + 3] & 0xFF);
			long total = (byte1 << 24) + (byte2 << 16) + (byte3 << 8) + byte4;

			pos = 10;
			byte1 = Math.abs(response[pos + 0] & 0xFF);
			byte2 = Math.abs(response[pos + 1] & 0xFF);
			byte3 = Math.abs(response[pos + 2] & 0xFF);
			byte4 = Math.abs(response[pos + 3] & 0xFF);
			long current = (byte1 << 24) + (byte2 << 16) + (byte3 << 8) + byte4;

			((CmdGetCalorieMissionGoalRes) _response).setCalorieMissionGoal(enableGoal > 0, goal, total, current);
			_response._success = true;
		} else {
			_response._success = false;
		}
		return PARSE_FINISHED;
	}

	public class CmdGetCalorieMissionGoalRes extends BandCommandResponse {
		protected boolean _enableGoal;
		protected long _goalCalories;
		protected long _totalCalories;
		protected long _currentCalories;

		public CmdGetCalorieMissionGoalRes() {
			_enableGoal = false;
			_goalCalories = 0;
			_totalCalories = 0;
			_currentCalories = 0;
		}

		public void setCalorieMissionGoal(boolean enableGoal, long goalCalories, long totalCalories, long currentCumulative) {
			_enableGoal = enableGoal;
			_goalCalories = goalCalories;
			_totalCalories = totalCalories;
			_currentCalories = currentCumulative;
		}

		@Override
		public byte getCommandCode() {
			return success_code;
		}

		public boolean enabledGoal() {
			return _enableGoal;
		}

		public long getGoalCalories() {
			return _goalCalories;
		}

		public long getTotalCalories() {
			return _totalCalories;
		}
	}

}
