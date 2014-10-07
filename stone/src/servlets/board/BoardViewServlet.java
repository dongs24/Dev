package servlets.board;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.board.BoardModel;
import dao.board.BoardDAOImpl;
import dao.board.BoardHibernateDAO;
//import dao.board.BoardMyBatisDAO;

@WebServlet("/board/boardViewServlet")
public class BoardViewServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private BoardDAOImpl boardDAO = null;

	public BoardViewServlet() {
		super();

	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// 파라미터
		String num = request.getParameter("num");
		String pageNum = request.getParameter("pageNum");
		String searchType = request.getParameter("searchType");
		String searchText = request.getParameter("searchText");
		// Model 객체에 파라미터값을 셋팅
		BoardModel boardModel = new BoardModel();
		boardModel.setNum(Integer.parseInt(num));
		boardModel.setPageNum(pageNum);
		boardModel.setSearchType(searchType);
		boardModel.setSearchText(searchText);
		// DAO
		// 일반 JDBC : this.boardDAO = newBoardDAO();
		// Mybatis :
		// this.boardDAO = new BoardMyBatisDAO();
		// Hibernate
		this.boardDAO = new BoardHibernateDAO();
		// 게시물 상세 조회 (DAO 호출)
		boardModel = this.boardDAO.select(boardModel);
		// 게시물 조회수 증가
		this.boardDAO.updateHit(boardModel);
		// View 사용될 객체 설정
		request.setAttribute("boardModel", boardModel);
		// View 보내기
		RequestDispatcher requestDispatcher = request
				.getRequestDispatcher("/WEB-INF/jsps/board/boardView.jsp");
		requestDispatcher.forward(request, response);
	}
}
