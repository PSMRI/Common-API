package com.iemr.common.model.excel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.iemr.common.data.email.StockAlertData;
import com.iemr.common.utils.exception.IEMRException;

/**
 * Covers the report workbook builder.
 *
 * <p>Each report kind writes a different set of filters onto the Criteria sheet, so the
 * generated workbook is read back with POI and the filter labels asserted per report.
 */
class ExcelHelperTest {

	private static final String[] HEADERS = { "SNo", "Call ID", "Call Date" };

	private static Criteria criteria() {
		Criteria criteria = new Criteria();
		criteria.setStart_Date("2024-01-01 00:00:00.000");
		criteria.setEnd_Date("2024-01-31 23:59:59.999");
		criteria.setService("1097");
		criteria.setAgentID("AG1");
		criteria.setRoleID("5");
		criteria.setCallTypeID("3");
		criteria.setCallTypeName("Medical Advice");
		criteria.setState("Karnataka");
		criteria.setDistrict("Bengaluru");
		criteria.setLanguage("Kannada");
		criteria.setCallerAgeGroup("10 - 20");
		criteria.setCall_Type("Inbound");
		criteria.setCall_Sub_Type("Advice");
		criteria.setGender("Female");
		criteria.setSexual_Orientation("Heterosexual");
		return criteria;
	}

	private static List<Object[]> rows() {
		List<Object[]> rows = new ArrayList<>();
		rows.add(new Object[] { "CALL1", "2024-01-05" });
		rows.add(new Object[] { "CALL2", null });
		return rows;
	}

	private static Workbook read(ByteArrayInputStream stream) throws Exception {
		return new XSSFWorkbook(stream);
	}

	/** The label written into the first column of each Criteria sheet row. */
	private static List<String> criteriaLabels(Workbook workbook) {
		Sheet sheet = workbook.getSheet("Criteria");
		List<String> labels = new ArrayList<>();
		for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
			labels.add(sheet.getRow(rowIndex).getCell(0).getStringCellValue());
		}
		return labels;
	}

	@Nested
	@DisplayName("tutorialsToExcel")
	class TutorialsToExcel {

		@Test
		@DisplayName("writes a Criteria sheet and a Report sheet")
		void writesBothSheets() throws Exception {
			try (Workbook workbook = read(ExcelHelper.tutorialsToExcel(HEADERS, rows(), "qareport", criteria()))) {
				assertThat(workbook.getSheet("Criteria")).isNotNull();
				assertThat(workbook.getSheet("Report")).isNotNull();
			}
		}

		@Test
		@DisplayName("writes the report headers and numbers each data row")
		void writesHeadersAndNumbersRows() throws Exception {
			try (Workbook workbook = read(ExcelHelper.tutorialsToExcel(HEADERS, rows(), "qareport", criteria()))) {
				Sheet report = workbook.getSheet("Report");

				assertThat(report.getRow(0).getCell(0).getStringCellValue()).isEqualTo("SNo");
				assertThat(report.getRow(0).getCell(1).getStringCellValue()).isEqualTo("Call ID");
				assertThat(report.getRow(1).getCell(0).getNumericCellValue()).isEqualTo(1d);
				assertThat(report.getRow(2).getCell(0).getNumericCellValue()).isEqualTo(2d);
				assertThat(report.getRow(1).getCell(1).getStringCellValue()).isEqualTo("CALL1");
			}
		}

		@Test
		@DisplayName("renders a null cell as an empty string")
		void nullCellBecomesEmpty() throws Exception {
			try (Workbook workbook = read(ExcelHelper.tutorialsToExcel(HEADERS, rows(), "qareport", criteria()))) {
				assertThat(workbook.getSheet("Report").getRow(2).getCell(2).getStringCellValue()).isEmpty();
			}
		}

		@Test
		@DisplayName("strips sub-second precision from the criteria dates")
		void stripsSubSecondPrecision() throws Exception {
			try (Workbook workbook = read(ExcelHelper.tutorialsToExcel(HEADERS, rows(), "qareport", criteria()))) {
				assertThat(workbook.getSheet("Criteria").getRow(1).getCell(1).getStringCellValue())
						.isEqualTo("2024-01-01 00:00:00");
			}
		}

		@Test
		@DisplayName("labels the Criteria header row")
		void labelsTheCriteriaHeader() throws Exception {
			try (Workbook workbook = read(ExcelHelper.tutorialsToExcel(HEADERS, rows(), "qareport", criteria()))) {
				assertThat(workbook.getSheet("Criteria").getRow(0).getCell(0).getStringCellValue())
						.isEqualTo("Filter Name");
				assertThat(workbook.getSheet("Criteria").getRow(0).getCell(1).getStringCellValue()).isEqualTo("Value");
			}
		}

		@ParameterizedTest(name = "{0} lists {1} filters")
		@CsvSource({ "qareport, 4", "Call_Summary_Report, 7", "Sexual_Orientation_Report, 5",
				"District_Wise_Call_Volume_Report, 2", "Call_Type_Report, 9", "Gender_Distribution_Report, 5",
				"Language_Distribution_Report, 5", "Caller_Age_Group_Report, 5", "Something_Unrecognised, 3" })
		@DisplayName("each report kind writes its own set of filters")
		void eachReportKindWritesItsFilters(String reportName, int expectedFilterCount) throws Exception {
			try (Workbook workbook = read(ExcelHelper.tutorialsToExcel(HEADERS, rows(), reportName, criteria()))) {
				assertThat(criteriaLabels(workbook)).hasSize(expectedFilterCount);
			}
		}

		@Test
		@DisplayName("the QA report lists the date range, role and agent")
		void qaReportFilters() throws Exception {
			try (Workbook workbook = read(ExcelHelper.tutorialsToExcel(HEADERS, rows(), "qareport", criteria()))) {
				assertThat(criteriaLabels(workbook)).containsExactly("Start_Date", "End_Date", "RoleID", "AgentID");
			}
		}

		@Test
		@DisplayName("the call summary report lists the service and call type filters too")
		void callSummaryReportFilters() throws Exception {
			try (Workbook workbook = read(
					ExcelHelper.tutorialsToExcel(HEADERS, rows(), "Call_Summary_Report", criteria()))) {
				assertThat(criteriaLabels(workbook)).containsExactly("Start_Date", "End_Date", "Service", "RoleID",
						"AgentID", "CallTypeName", "CallTypeId");
			}
		}

		@Test
		@DisplayName("the call type report lists every demographic filter")
		void callTypeReportFilters() throws Exception {
			try (Workbook workbook = read(
					ExcelHelper.tutorialsToExcel(HEADERS, rows(), "Call_Type_Report", criteria()))) {
				assertThat(criteriaLabels(workbook)).containsExactly("Start_Date", "End_Date", "State", "District",
						"Language", "Call_Type", "Call_Sub_Type", "Gender", "Sexual_Orientation");
			}
		}

		@Test
		@DisplayName("an unrecognised report falls back to the date range and service")
		void unrecognisedReportFallsBack() throws Exception {
			try (Workbook workbook = read(
					ExcelHelper.tutorialsToExcel(HEADERS, rows(), "Not_A_Known_Report", criteria()))) {
				assertThat(criteriaLabels(workbook)).containsExactly("Start_Date", "End_Date", "Service");
			}
		}

		@Test
		@DisplayName("produces a Report sheet with only headers when there are no rows")
		void handlesAnEmptyResultSet() throws Exception {
			try (Workbook workbook = read(
					ExcelHelper.tutorialsToExcel(HEADERS, new ArrayList<>(), "qareport", criteria()))) {
				assertThat(workbook.getSheet("Report").getLastRowNum()).isZero();
			}
		}

		@Test
		@DisplayName("fails with a clear message when the criteria dates are missing")
		void failsWhenCriteriaDatesAreMissing() {
			assertThatExceptionOfType(IEMRException.class)
					.isThrownBy(() -> ExcelHelper.tutorialsToExcel(HEADERS, rows(), "qareport", new Criteria()))
					.withMessageContaining("fail to import data to Excel file");
		}
	}

	@Nested
	@DisplayName("isValidDate")
	class IsValidDate {

		@ParameterizedTest(name = "\"{0}\" is a date")
		@ValueSource(strings = { "2024-01-05", "1999-12-31" })
		@DisplayName("accepts a yyyy-MM-dd date")
		void acceptsIsoDates(String input) {
			assertThat(ExcelHelper.isValidDate(input)).isTrue();
		}

		@ParameterizedTest(name = "\"{0}\" is not a date")
		@ValueSource(strings = { "", "CALL1", "not-a-date", "abc-de-fg" })
		@DisplayName("rejects text that is not a date")
		void rejectsNonDates(String input) {
			assertThat(ExcelHelper.isValidDate(input)).isFalse();
		}
	}

	@Nested
	@DisplayName("InventoryDataToExcel")
	class InventoryDataToExcel {

		private StockAlertData stockAlert() {
			StockAlertData data = new StockAlertData();
			data.setItemName("Paracetamol");
			data.setTotalquantity(500);
			data.setQuantityInHand(40);
			data.setQuantityinhandPercent(8.0d);
			data.setFacilityName("PHC Anekal");
			data.setDistrictName("Bengaluru");
			return data;
		}

		@Test
		@DisplayName("writes a Stock Alert sheet with a labelled header row")
		void writesTheHeaderRow() throws Exception {
			byte[] bytes = ExcelHelper.InventoryDataToExcel(List.of(stockAlert()));

			try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
				Sheet sheet = workbook.getSheet("Stock Alert");

				assertThat(sheet).isNotNull();
				assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Drug Name");
				assertThat(sheet.getRow(0).getCell(3).getStringCellValue()).isEqualTo("% Stock Consumed");
				assertThat(sheet.getRow(0).getCell(5).getStringCellValue()).isEqualTo("District Name");
			}
		}

		@Test
		@DisplayName("writes one row per stock alert")
		void writesOneRowPerAlert() throws Exception {
			byte[] bytes = ExcelHelper.InventoryDataToExcel(List.of(stockAlert(), stockAlert()));

			try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
				Sheet sheet = workbook.getSheet("Stock Alert");

				assertThat(sheet.getLastRowNum()).isEqualTo(2);
				assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("Paracetamol");
				assertThat(sheet.getRow(1).getCell(1).getNumericCellValue()).isEqualTo(500d);
				assertThat(sheet.getRow(1).getCell(4).getStringCellValue()).isEqualTo("PHC Anekal");
			}
		}

		@Test
		@DisplayName("produces a header-only sheet for an empty stock list")
		void handlesAnEmptyList() throws Exception {
			byte[] bytes = ExcelHelper.InventoryDataToExcel(List.of());

			try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
				assertThat(workbook.getSheet("Stock Alert").getLastRowNum()).isZero();
			}
		}

		@Test
		@DisplayName("fails with a clear message when a stock alert cannot be written")
		void failsOnUnwritableData() {
			assertThatExceptionOfType(Exception.class)
					.isThrownBy(() -> ExcelHelper.InventoryDataToExcel(java.util.Collections.singletonList(null)))
					.withMessageContaining("fail to import data to Excel file");
		}
	}

	@Test
	@DisplayName("the helper can be constructed")
	void isConstructible() {
		assertThat(new ExcelHelper()).isNotNull();
	}
}
