package com.isesol.mes.ismes.em.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;
import com.isesol.mes.ismes.em.constant.TableConstant;
import com.isesol.mes.ismes.em.util.DateTimeUtils;

import net.sf.json.JSONArray;

public class SbtzActivity {

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat sdf_time = new SimpleDateFormat("yyyyMMddHHmmss");
	private SimpleDateFormat sdf_timea = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Logger log4j = Logger.getLogger(SbtzActivity.class);

	/**
	 * 跳转设备台账页面
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String query_sbtz(Parameters parameters, Bundle bundle) {
		return "em_sbtz";
	}

	public void testForPaiChan(Parameters parameters, Bundle bundle) {
		String sbid = parameters.getString("sbid");// "5010"
		String begin = parameters.getString("begin");
		String end = parameters.getString("end");
		if (StringUtils.isBlank(sbid) || StringUtils.isBlank(begin) || StringUtils.isBlank(end)) {
			return ;
		}
		parameters.set("begin", DateTimeUtils.string2Date(begin));
		parameters.set("end", DateTimeUtils.string2Date(end));
		Bundle b_fm = Sys.callModuleService("em", "query_sb_cannotJobTime_service", parameters);
		StringBuffer sb = new StringBuffer();
		List<Map<String, Object>> list1 = (List<Map<String, Object>>) b_fm.get("list");
		System.out.println("========list=========");
		int i = 1;
		for (Map<String, Object> m : list1) {
			System.out.println("*******第" + i + "个******");
			for (Entry<String, Object> entry : m.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (value instanceof Date) {
					value = DateTimeUtils.date2String((Date) value, "yyyy-MM-dd HH:mm:ss");
				}
				System.out.println(key + "====" + value);
			}
			i++;
		}
		System.out.println("========AllDayList=========");
		List<Map<String, Object>> list2 = (List<Map<String, Object>>) b_fm.get("allDaylist");
		for (Map<String, Object> m : list2) {
			System.out.println("*******第" + i + "个******");
			for (Entry<String, Object> entry : m.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (value instanceof Date) {
					value = DateTimeUtils.date2String((Date) value, "yyyy-MM-dd HH:mm:ss");
				}
				System.out.println(key + "====" + value);
			}

		}
		i++;
	}

	/**
	 * 插入/修改设备信息表
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String update_sbtz(Parameters parameters, Bundle bundle) throws Exception {
		bundle.put("code", "0");
		List<Map<String, Object>> sbtz_inlist = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> sbtz_uplist = new ArrayList<Map<String, Object>>();
		List<Object[]> objlist = new ArrayList<Object[]>();
		JSONArray jsonarray = JSONArray.fromObject(parameters.get("data_list"));
		for (int i = 0; i < jsonarray.size(); i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			Map<String, Object> map_in = new HashMap<String, Object>();
			map = jsonarray.getJSONObject(i);
			map_in.put("jyrq", sdf.parse(map.get("jyrq").toString()));
			map_in.put("sbbh", map.get("sbbh"));
			map_in.put("sbmc", map.get("sbmc"));
			map_in.put("sblxid", map.get("sblxid"));
			map_in.put("sbwz", map.get("sbwz"));
			map_in.put("ccbh", map.get("ccbh"));
			map_in.put("ip", map.get("ip"));
			map_in.put("cj", map.get("cj"));
			map_in.put("zzjgid", map.get("zzjgid"));
			map_in.put("sbtpid", map.get("sbtpid"));

			if (StringUtils.isNotBlank(map.get("addSign").toString())) {
				Dataset ds = Sys.query(TableConstant.设备信息表, "sbbh", "sbbh=?", null, new Object[]{map.get("sbbh")});
				if(null != ds && ds.getCount() > 0){
					bundle.put("code", "1");//0:成功，1:失败
					bundle.put("message", "设备编号重复，请重新输入！");
					return "json:";
				}
				sbtz_inlist.add(map_in);

			} else {
				Dataset ds = Sys.query(TableConstant.设备信息表, "sbbh", "sbbh=? and sbid<> ?", null, new Object[]{map.get("sbbh"),map.get("sbid")});
				if(null != ds && ds.getCount() > 0){
					bundle.put("code", "1");//0:成功，1:失败
					bundle.put("message", "设备编号重复，请重新输入！");
					return "json:";
				}
				objlist.add(new Object[] { map.get("sbid") });
				sbtz_uplist.add(map_in);
			}
		}
		if (sbtz_inlist.size() > 0) {
			try {
				int i = Sys.insert(TableConstant.设备信息表, sbtz_inlist);
				System.out.println("插入数量" + i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (sbtz_uplist.size() > 0) {
			try {
				int i = Sys.update(TableConstant.设备信息表, sbtz_uplist, "sbid=?", objlist);
				System.out.println("更新数量" + i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "json:";
	}

	/**
	 * 删除设备信息表
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String del_sbtz(Parameters parameters, Bundle bundle) {
		if (StringUtils.isBlank(parameters.getString("data_list"))) {
			return "json:";
		}
		List<Object[]> objlist = new ArrayList<Object[]>();
		objlist.add(new Object[] { Integer.parseInt(parameters.getString("data_list")) });
		try {
			int i = Sys.delete(TableConstant.设备信息表, "sbid=?", objlist);
			System.out.println("删除数量" + i);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "json:";
	}

	/**
	 * 查询设备信息表
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 * @throws Exception
	 */
	public String table_sbtz(Parameters parameters, Bundle bundle) throws Exception {

		// 查询设备信息表
		String query_sbbh = parameters.getString("query_sbbh");
		String query_sbmc = parameters.getString("query_sbmc");
		String query_sbfl = parameters.getString("query_sbfl");
		String query_sblx = parameters.getString("query_sblx");
		String query_sbwz = parameters.getString("query_sbwz");
		String query_ccbh = parameters.getString("query_ccbh");
		String query_ip = parameters.getString("query_ip");
		String query_cj = parameters.getString("query_cj");
		String query_jyrqstart = parameters.getString("query_jyrqstart");
		String query_jyrqend = parameters.getString("query_jyrqend");
		String query_ssjg = parameters.getString("query_ssjg");
		String sortName = parameters.getString("sortName");// 排序字段
		String sortOrder = parameters.getString("sortOrder");// 排序方式 asc desc

		String con = "1 = 1 ";
		List<Object> val = new ArrayList<Object>();
		if (StringUtils.isNotBlank(query_sbbh)) {
			con = con + " and sbbh like ? ";
			val.add("%" + query_sbbh + "%");
		}
		if (StringUtils.isNotBlank(query_sbmc)) {
			con = con + " and sbmc like ? ";
			val.add("%" + query_sbmc + "%");
		}
		if (StringUtils.isNotBlank(query_sblx)) {
			con = con + " and em_sbxxb.sblxid = ? ";
			val.add(query_sblx);
		}
		if (StringUtils.isNotBlank(query_sbwz)) {
			con = con + " and sbwz like ? ";
			val.add("%" + query_sbwz + "%");
		}
		if (StringUtils.isNotBlank(query_ccbh)) {
			con = con + " and ccbh like ? ";
			val.add("%" + query_ccbh + "%");
		}
		if (StringUtils.isNotBlank(query_ip)) {
			con = con + " and ip like ? ";
			val.add("%" + query_ip + "%");
		}
		if (StringUtils.isNotBlank(query_cj)) {
			con = con + " and cj like ? ";
			val.add("%" + query_cj + "%");
		}
		if (StringUtils.isNotBlank(query_jyrqstart)) {
			con = con + " and jyrq >=?  ";
			val.add(sdf_timea.parse(query_jyrqstart+" 00:00:00"));
		}
		if (StringUtils.isNotBlank(query_jyrqend)) {
			con = con + " and jyrq <=? ";
			val.add(sdf_timea.parse(query_jyrqend+" 23:59:59"));
		}
		if (StringUtils.isNotBlank(query_ssjg)) {
			con = con + " and zzjgid = ? ";
			val.add(query_ssjg);
		}
		if (StringUtils.isNotBlank(query_sbfl)) {
			con = con + " and sbfldm = ? ";
			val.add(query_sbfl);
		}
		if (StringUtils.isNotBlank(sortName)) {
			sortOrder = sortName + " " + sortOrder + " ";
		} else {
			sortOrder = "jyrq desc";
		}

		int page = Integer.parseInt(parameters.get("page").toString());
		int pageSize = Integer.parseInt(parameters.get("pageSize").toString());
		Dataset datasetsbtz = Sys.query(new String[] { "em_sbxxb", "em_sblxb" },
				"em_sbxxb left join em_sblxb on em_sbxxb.sblxid = em_sblxb.sblxid",
				"sbid,sbbh,sbmc,em_sbxxb.sblxid,sblxmc,ccbh,ip,cj,zzjgid,jyrq,sbwz,kfid,sbfldm,sbtpid", con, sortOrder,
				(page - 1) * pageSize, pageSize, val.toArray());
		List<Map<String, Object>> sbtz = datasetsbtz.getList();
		if (CollectionUtils.isNotEmpty(sbtz)) {
			for (Map<String, Object> m : sbtz) {
				parameters.set("id", m.get("zzjgid"));
				Bundle b_org = Sys.callModuleService("org", "nameService", parameters);
				if (b_org != null && b_org.get("name") != null
						&& StringUtils.isNotBlank(b_org.get("name").toString())) {
					m.put("zzjgmc", b_org.get("name").toString());
				}
			}
		}
		bundle.put("rows", sbtz);
		int totalPage = datasetsbtz.getTotal() % pageSize == 0 ? datasetsbtz.getTotal() / pageSize
				: datasetsbtz.getTotal() / pageSize + 1;
		bundle.put("totalPage", totalPage);
		bundle.put("currentPage", page);
		bundle.put("totalRecord", datasetsbtz.getTotal());
		return "json:";
	}

	@SuppressWarnings("unchecked")
	public String get_cj(Parameters parameters, Bundle bundle) {
		Bundle b_cj = Sys.callModuleService("org", "cjService", parameters);
		List<Map<String, Object>> cjxx = (List<Map<String, Object>>) b_cj.get("data");
		for (int i = 0; i < cjxx.size(); i++) {
			cjxx.get(i).put("value", cjxx.get(i).get("zzjgid"));
			cjxx.get(i).put("label", cjxx.get(i).get("zzjgmc"));
			cjxx.get(i).remove("zzjgid");
			cjxx.get(i).remove("zzjgmc");
		}
		bundle.put("cjxx", cjxx);
		return "json:cjxx";
	}

	/**
	 * 得到一个设备的设备工时
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String getSbgs(Parameters parameters, Bundle bundle) {
		String sbid = parameters.getString("sbid");
		if (StringUtils.isBlank(sbid)) {
			return "json:";
		}
		int page = Integer.parseInt(parameters.get("page").toString());
		int pageSize = Integer.parseInt(parameters.get("pageSize").toString());
		Dataset dataset = Sys.query("em_sbgsb", "sbgsid,sbid,gsmbid,ksrq,jsrq", " sbid = ? ", null,
				(page - 1) * pageSize, pageSize, new Object[] { sbid });
		int totalPage = dataset.getTotal() % pageSize == 0 ? dataset.getTotal() / pageSize
				: dataset.getTotal() / pageSize + 1;
		List<Map<String, Object>> list = dataset.getList();
		if (CollectionUtils.isNotEmpty(list)) {
			for (Map<String, Object> map : list) {
				parameters.set("gsmbid", map.get("gsmbid"));
				Bundle b_fm = Sys.callModuleService("fm", "fmService_query_gcgsmbb", parameters);
				Map<String, Object> map_gcgsmb = (Map<String, Object>) b_fm.get("gcgsmbb");
				if (MapUtils.isNotEmpty(map_gcgsmb)) {
					map.put("gsmbmc", map_gcgsmb.get("gsmbmc"));

					map.put("gsmbms", map_gcgsmb.get("gsmbms") == null ? "" : map_gcgsmb.get("gsmbms"));
				}
			}
		}

		bundle.put("totalPage", totalPage);
		bundle.put("currentPage", page);
		bundle.put("totalRecord", dataset.getTotal());
		bundle.put("rows", list);

		return "json:";
	}

	private boolean ifTime(String sbgsid) {
		try {
			Date date = new Date(Long.valueOf(sbgsid));
			if (date.compareTo(DateTimeUtils.string2Date("2016-07-01")) < 0) {
				return false;
			}
			if (date.compareTo(DateTimeUtils.string2Date("2050-12-31")) > 0) {
				return false;
			}
			return true;

		} catch (Exception e) {
			return false;
		}

	}

	/**
	 * 保存设备工时
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String saveSbgs(Parameters parameters, Bundle bundle) {
		String sbids = parameters.getString("sbids");
		if (StringUtils.isBlank(sbids)) {
			return "json:data";
		}
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		List<Object[]> conditionValue = new ArrayList<Object[]>();
		String sbgsid = parameters.getString("sbgsid");
		boolean insertFlag = ifTime(sbgsid);
		String gsmbid = parameters.getString("gsmbid");
		String ksrq_str = parameters.getString("ksrq");
		String jsrq_str = parameters.getString("jsrq");
		for (String sbid : sbids.split(",")) {
			Map<String, Object> map = new HashMap<String, Object>();
			conditionValue.add(new Object[] { sbgsid });
			map.put("gsmbid", gsmbid);
			map.put("ksrq", DateTimeUtils.string2Date(ksrq_str));
			map.put("jsrq", DateTimeUtils.string2Date(jsrq_str));

			map.put("sbid", sbid);
			list.add(map);

			if (insertFlag) {
				Sys.insert("em_sbgsb", map);
			} else {
				Sys.update("em_sbgsb", map, " sbgsid = ? ", new Object[] { sbgsid });
			}
		}

		return "json:data";
	}

	/**
	 * 删除 设备工时
	 * 
	 * @param parameters
	 * @param bundle
	 */
	public void delSbmb(Parameters parameters, Bundle bundle) {
		String sbgsid = parameters.getString("sbgsid");
		boolean exitsFlag = !ifTime(sbgsid);
		if (exitsFlag) {
			Sys.delete("em_sbgsb", " sbgsid = ? ", sbgsid);
		}
	}

	/**
	 * 选择工时模版
	 * 
	 * @param parameters
	 * @param bundle
	 */
	public String gsmb_select(Parameters parameters, Bundle bundle) {
		Bundle b_fm = Sys.callModuleService("fm", "fmService_query_gcgsmbb", parameters);
		List<Map<String, Object>> list = (List<Map<String, Object>>) b_fm.get("gcgsmbbList");
		List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
		if (CollectionUtils.isNotEmpty(list)) {
			for (Map<String, Object> map : list) {
				Map<String, Object> newMap = new HashMap<String, Object>();
				newMap.put("label", map.get("gsmbmc"));
				newMap.put("value", map.get("gsmbid"));
				returnList.add(newMap);
			}
		}
		bundle.put("gsmb_list", returnList);

		return "json:gsmb_list";
	}

	public String gsmbinfo(Parameters parameters, Bundle bundle) {
		Bundle b_fm = Sys.callModuleService("fm", "fmService_query_gcgsmbb", parameters);
		bundle.put("data", b_fm.get("gcgsmbb"));
		return "json:data";
	}

	public String getCalendarDays(Parameters parameters, Bundle bundle) {
		String sbid = parameters.getString("sbid");

		String start = parameters.getString("start");
		String end = parameters.getString("end");
		List<Map<String, String>> events = new ArrayList<Map<String, String>>();

		// 得到所有周末
		Map<String, Object> map = weekdaysList(start, end);
		List<String> weekendsList = (List<String>) map.get("weekendsList");

		if (StringUtils.isNotBlank(sbid)) {
			Dataset dataset = Sys.query("em_sbrlb", "rlzt,rlrq", " sbid = ? ", null, new Object[] { sbid });
			List<Map<String, Object>> specialDays = dataset.getList();
			if (CollectionUtils.isNotEmpty(specialDays)) {
				for (int i = 0; i < specialDays.size(); i++) {
					Map<String, Object> m = specialDays.get(i);
					String rlztdm = m.get("rlzt").toString();
					Date rlrq = (Date) m.get("rlrq");
					String rlrq_str = DateTimeUtils.date2String(rlrq, "yyyy-MM-dd");
					// 工作日
					if ("10".equals(rlztdm)) {
						if (weekendsList.contains(rlrq_str)) {
							weekendsList.remove(rlrq_str);
						}
					}
					// 休息日
					if ("20".equals(rlztdm) || "30".equals(rlztdm) || "40".equals(rlztdm) || "50".equals(rlztdm)) {
						if (!weekendsList.contains(rlrq_str)) {
							weekendsList.add(rlrq_str);
						}
					}
				}
			}
		}

		for (String s : weekendsList) {
			events.add(backgroundMap(s));
		}

		bundle.put("data", events);
		return "json:data";
	}

	/**
	 * weekendsList ---List<String> 周末的 数组
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	private Map<String, Object> weekdaysList(String start, String end) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Date startDate = DateTimeUtils.string2Date(start);
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		Date endDate = DateTimeUtils.string2Date(end);
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(endDate);

		List<String> weekendsList = new ArrayList<String>();
		while (startCal.getTime().compareTo(endCal.getTime()) < 1) {
			int weekday = startCal.get(Calendar.DAY_OF_WEEK);
			String month = String.valueOf(startCal.get(Calendar.MONTH) + 1);
			month = month.length() == 1 ? "0" + month : month;
			String day = String.valueOf(startCal.get(Calendar.DAY_OF_MONTH));
			day = day.length() == 1 ? "0" + day : day;
			String str = String.valueOf(startCal.get(Calendar.YEAR)) + "-" + month + "-" + day;
			if (weekday == Calendar.SATURDAY) {
				weekendsList.add(str);
			}
			if (weekday == Calendar.SUNDAY) {
				weekendsList.add(str);
			}

			startCal.add(Calendar.DAY_OF_MONTH, 1);
		}

		returnMap.put("weekendsList", weekendsList);
		return returnMap;
	}

	private Map<String, String> backgroundMap(String s) {
		Map<String, String> m = new HashMap<String, String>();
		m.put("start", s);
		m.put("backgroundColor", "blue");
		m.put("rendering", "background");
		return m;
	}

	/**
	 * 保存设备日历
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String saveSbrl(Parameters parameters, Bundle bundle) {
		String rlrq_s = parameters.getString("sbrl_rl");
		List<Date> rlrqList = new ArrayList<Date>();
		if (rlrq_s.contains("-")) {
			String beginStr = rlrq_s.split("-")[0];
			String endStr = rlrq_s.split("-")[1];

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(DateTimeUtils.string2Date(beginStr));
			while (calendar.getTime().compareTo(DateTimeUtils.string2Date(endStr)) < 1) {
				rlrqList.add(calendar.getTime());
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
		} else {
			rlrqList.add(DateTimeUtils.string2Date(rlrq_s));
		}
		List<String> sbidStrList = new ArrayList<String>();
		String sbids = parameters.getString("hidden_sbids");
		for (String s_id : sbids.split(",")) {
			if (StringUtils.isNotBlank(s_id)) {
				sbidStrList.add(s_id);
			}
		}
		List<Map<String, Object>> insertList = new ArrayList<Map<String, Object>>();
		List<Object[]> deleteList = new ArrayList<Object[]>();
		List<String> returnDateStr = new ArrayList<String>();
		String rlzt = parameters.getString("sbrl_zt");
		String rlms = parameters.getString("sbrl_ms");
		for (Date d : rlrqList) {
			for (String sbid : sbidStrList) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("rlrq", d);
				map.put("rlzt", rlzt);
				map.put("sbid", sbid);
				map.put("rlms", rlms);
				insertList.add(map);

				returnDateStr.add(DateTimeUtils.date2String(d, "yyyy/MM/dd"));

				deleteList.add(new Object[] { sbid, d });
				Sys.delete("em_sbrlb", " sbid = ? and rlrq = ? ", new Object[] { sbid, d });
			}

		}
		Sys.insert("em_sbrlb", insertList);

		bundle.put("data", returnDateStr.toArray());
		return "json:data";
	}

	/**
	 * 设备日历点击 日期
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String calendarSelect(Parameters parameters, Bundle bundle) {
		String sbids = parameters.getString("sbids");
		String rlrq = parameters.getString("rlrq");
		Map<String, Object> map = null;
		if (StringUtils.isNotBlank(sbids) && sbids.split(",").length < 2) {
			String sbid = sbids.split(",")[0];
			Dataset dataset = Sys.query("em_sbrlb", "rlzt,rlms", " sbid = ? and rlrq = ? ", null,
					new Object[] { sbid, DateTimeUtils.string2Date(rlrq) });
			map = dataset.getMap();
		}
		if (MapUtils.isEmpty(map) || StringUtils.isBlank(sbids) || sbids.split(",").length > 2) {
			Map<String, Object> returnMap = weekdaysList(rlrq, rlrq);
			List<String> weekendsList = (List<String>) returnMap.get("weekendsList");
			if (weekendsList.contains(rlrq)) {
				map = new HashMap<String, Object>();
				map.put("rlzt", "20");
				map.put("rlms", "周末");
			}
		}

		bundle.put("data", map);
		return "json:data";

	}
	/**上传设备图片
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String sbtpUpload(Parameters parameters, Bundle bundle) {
		String cxmc = parameters.getFile("add_sbtp").getName();
		String ContentType = parameters.getFile("add_sbtp").getContentType();
		long   wjdx = parameters.getFile("add_sbtp").getSize();
		String filetype = cxmc.substring((cxmc.lastIndexOf(".")),cxmc.length());
		String sbid = parameters.get("add_sbid").toString();
		String add_Sign = parameters.get("add_Sign").toString();
		String wjbcmc="sbxx" + sdf_time.format(new Date())+filetype;
		Map<String, Object> map = new HashMap<String, Object>(); 
		map.put("wjlj", "/sbtz/"+wjbcmc);
		map.put("wjmc", wjbcmc);
		map.put("wjdx", wjdx);
		map.put("wjlb", ContentType);
		parameters.set("infoMap", map); 
		Sys.saveFile("/sbtz/"+wjbcmc, parameters.getFile("add_sbtp").getInputStream());
		Bundle b_file = Sys.callModuleService("pm", "pmservice_insertFile", parameters);
		Map<String, Object> sbmap = new HashMap<String, Object>(); 
		sbmap.put("sbtpid", b_file.get("wjid"));
		if (!"1".equals(add_Sign)) {
			Sys.update("em_sbxxb", sbmap, " sbid = ? ", new Object[]{sbid});
		}
		bundle.put("sbtpid", b_file.get("wjid"));
		return "json:sbtpid";
	}
	
}
