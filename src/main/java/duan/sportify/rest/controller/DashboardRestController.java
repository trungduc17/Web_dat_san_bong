package duan.sportify.rest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import duan.sportify.dao.BookingDAO;
import duan.sportify.dao.BookingDetailDAO;
import duan.sportify.dao.ContactDAO;
import duan.sportify.dao.EventDAO;
import duan.sportify.dao.FieldDAO;
import duan.sportify.dao.UserDAO;
import duan.sportify.entities.Bookingdetails;
import duan.sportify.entities.Contacts;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/rest/dashboard/")
public class DashboardRestController {
	@Autowired
	BookingDAO bookingDAO;
	@Autowired
	FieldDAO fieldDAO;
	@Autowired
	UserDAO userDAO;
	@Autowired
	EventDAO eventDAO;
	@Autowired
	ContactDAO contactDAO;
	@Autowired
	BookingDetailDAO bookingDetailDAO;

	// tổng sân
	@GetMapping("totalField")
	public long countField() {
		return fieldDAO.count();
	}

	// tổng người dùng
	@GetMapping("totalUser")
	public long countUser() {
		return userDAO.count();
	}

	// tổng phiếu đặt trong ngày
	@GetMapping("totalOrderBooking")
	public long sumOrderBooking() {
		return bookingDAO.sumOrderBooking();
	}

	// barcharts
	// cột a
	@GetMapping("barcharts_a")
	public List<Object[]> totalPriceOn6YearReturn() {
		return bookingDAO.getBookingPriceSummary();
	}


	// linecharts
	// line a
	@GetMapping("linecharts_a")
	public List<Object[]> conutBookingOnDuring6Years() {
		return bookingDAO.countBookingOn6YearReturn();
	}


	// đếm số hoa đơn trong ngày
	@GetMapping("countBookingInDate")
	public Integer countBookingInDate() {
		return bookingDAO.countBookingInDate();
	}

	// đếm sân dang hoạt động
	@GetMapping("countFieldActiving")
	public Integer countFieldActiving() {
		return fieldDAO.countFieldActiving();
	}


	// thong ke booking trong ngay
	@GetMapping("thongkebookingtrongngay")
	public List<Object[]> thongkebookingtrongngay() {
		return bookingDAO.thongkebookingtrongngay();
	}

	// lấy danh sach 3 con tac trongg ngay
	@GetMapping("danhsach3contact")
	public List<Contacts> danhsach3contact() {
		return contactDAO.fill3ContactOnDate();
	}

	// dem liên hệ trong ngày
	@GetMapping("demLienHeTrongNgay")
	public int demLienHeTrongNgay() {
		return contactDAO.demLienHeTrongNgay();
	}

	// dem tổng số phiếu dat san tháng này vs thang trước
	@GetMapping("tongSoPhieuDatSan2Thang")
	public List<Object[]> tongSoPhieuDatSan2Thang() {
		return bookingDAO.tongSoPhieuDatSan2Thang();
	}


	// dem tổng dat san doanh thu tháng này vs thang trước
	@GetMapping("tongDoanhThuBooking2Month")
	public List<Object[]> tongDoanhThuBooking2Month() {
		return bookingDAO.tongDoanhThuBooking2Month();
	}


	// top 3 san dat nhiều nhất
		@GetMapping("top3SanDatNhieu")
		public List<Object[]> top3SanDatNhieu() {
			return bookingDetailDAO.top3SanDatNhieu();
		}

	// top 5 user dat san nhiều nhat
	
	@GetMapping("top5UserDatSan")
	public List<Object[]> top5UserDatSan(){
		return bookingDetailDAO.top5UserDatSan();
	}

}
