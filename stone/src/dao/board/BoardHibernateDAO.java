package dao.board;

import hibernate.Hibernate;

import java.util.List;

import model.board.BoardModel;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import util.ComUtil;


/**
 * 게시판 Hibernate DAO 클래스
 * @since 2013.07.29
 * @author stoneis.pe.kr
 */
public class BoardHibernateDAO implements BoardDAOImpl {
	
	/** Hibernate SQL 팩토리 */
	private SessionFactory sessionFactory = null;
	
	public BoardHibernateDAO() {
		this.sessionFactory = Hibernate.getSessionFactory();
	}
	
	/**
	 * 게시판 목록 조회
	 * @param boardModel
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<BoardModel> selectList(BoardModel boardModel) {
		Session session = this.sessionFactory.openSession();
		try {
			//직관적인 Criteria Query API 사용한 경우! 
			Criteria criteria = session.createCriteria(BoardModel.class);
			//행의 범위 설정 LIMIT ?,? 값을 의미
			criteria.setMaxResults(boardModel.getListCount());
			criteria.setFirstResult(boardModel.getStartIndex());
			//검색어
			String searchText = boardModel.getSearchText();
			String searchType = boardModel.getSearchType();
			//검색어 파라미터가 존재할 시
			if (!"".equals(searchText)) {
				//LIKE 검색을 위한 메소드
				Criterion subject = Restrictions.like("subject", "%"+searchText+"%");
				Criterion writer = Restrictions.like("writer", "%"+searchText+"%");
				Criterion contents = Restrictions.like("contents", "%"+searchText+"%");
				if ("ALL".equals(searchType)) {
					criteria.add(Restrictions.or(subject, writer, contents));
				} else if ("SUBJECT".equals(searchType)) {
					criteria.add(subject);
				} else if ("WRITER".equals(searchType)) {
					criteria.add(writer);
				} else if ("CONTENTS".equals(searchType)) {
					criteria.add(contents);
				}
			}
			//등록일시로 정렬 한다.(*컬럼명이 아니라 멤버변수)
			criteria.addOrder(Order.desc("regDate"));
			return criteria.list();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//사용한 세션을 종료
			if (session != null) session.close();
		}
		return null;
	}
	
	/**
	 * 게시판 수 조회
	 * @param boardModel
	 * @return
	 */
	public int selectCount(BoardModel boardModel) {
		Session session = this.sessionFactory.openSession();
		try {
			Criteria criteria = session.createCriteria(BoardModel.class);
			String searchText = boardModel.getSearchText();
			String searchType = boardModel.getSearchType();
			if (!"".equals(searchText)) {
				Criterion subject = Restrictions.like("subject", "%"+searchText+"%");
				Criterion writer = Restrictions.like("writer", "%"+searchText+"%");
				Criterion contents = Restrictions.like("contents", "%"+searchText+"%");
				if ("ALL".equals(searchType)) {
					criteria.add(Restrictions.or(subject, writer, contents));
				} else if ("SUBJECT".equals(searchType)) {
					criteria.add(subject);
				} else if ("WRITER".equals(searchType)) {
					criteria.add(writer);
				} else if ("CONTENTS".equals(searchType)) {
					criteria.add(contents);
				}
			}
			//COUNT() 쿼리로 해주는 메소드
			criteria.setProjection(Projections.rowCount());
			//결과값을 INT형 변환한다.
			return ((Long)criteria.uniqueResult()).intValue();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null) session.close();
		}
		return 0;
	}
	
	/**
	 * 게시판 상세 조회
	 * @param boardModel
	 * @return
	 */
	public BoardModel select(BoardModel boardModel) {
		Session session = this.sessionFactory.openSession();
		try {
			//get() 메소드를 통해 모델과 board.xml에서 선언한 <id/>와 매칭할 값을 인자로 던짐
			return (BoardModel)session.get(BoardModel.class, boardModel.getNum());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null) session.close();
		}
		return null;
	}
	
	/**
	 * 게시판 등록 처리
	 * @param boardModel
	 */
	public void insert(BoardModel boardModel) {
		Session session = this.sessionFactory.openSession();
		//INSERT,UPDATE,DELETE 사용시 트랜젝션을 얻어온다.
		Transaction transaction = session.beginTransaction();
		try {
			//save() 메소드만 실행시키면 간단히 해결된다.
			//등록일시, 수정일시를 현재날짜를 설정
			boardModel.setRegDate(ComUtil.getDate());
			boardModel.setModDate(ComUtil.getDate());
			session.save("BoardModel", boardModel);
			//커밋
			transaction.commit();
		} catch (Exception e) {
			//롤백
			transaction.rollback();
			e.printStackTrace();
		} finally {
			if (session != null) session.close();
		}
	}
	
	/**
	 * 게시판 수정 처리
	 * @param boardModel
	 */
	public void update(BoardModel boardModel) {
		Session session = this.sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			//기존의 값을 조회
			BoardModel oldBoardModel = (BoardModel)session.get(BoardModel.class, boardModel.getNum());
			//변경된 내용을 기존의 BoardModel 설정
			oldBoardModel.setSubject(boardModel.getSubject());
			oldBoardModel.setContents(boardModel.getContents());
			oldBoardModel.setWriter(boardModel.getWriter());
			oldBoardModel.setIp(boardModel.getIp());
			oldBoardModel.setModDate(ComUtil.getDate());
			/*변경된 내용이 기존을 BoardModel에 설정한 것만으로도 UPDATE가 이루어진다.
			 * board.xml 에서<id/>와 num 값이 매핑되어 수정
			 * 어떻게 보면 이부분이 ORM 기술을 뚜렷히 볼 수 있는 부분!
			 * 필히 기존의 값을 조회해 와서 변경된 내용을 설정해야 된다.
			 * 하지만 원하는 컬럼만 수정하고 싶을 때가 생길수도..
			 */
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			e.printStackTrace();
		} finally {
			if (session != null) session.close();
		}
	}
	
	/**
	 * 게시판 조회수 증가 수정 처리
	 * @param boardModel
	 */
	public void updateHit(BoardModel boardModel) {
		Session session = this.sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			//조회수 1 증가
			boardModel.setHit(boardModel.getHit()+1);
			//위 수정처리와는 다르게 update() 메소드를 활용
			//위에서 하는 방법도 있고 이러한 방법도 있으므로 다르게 구현했다.
			session.update(boardModel);
			//SQLQuery sql = session.createSQLQuery("UPDATE Board SET hit = hit +1 WHERE num = :num");
			//sql.setParameter("num", boardModel.getNum());
			//sql.executeUpdate();
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			e.printStackTrace();
		} finally {
			if (session != null) session.close();
		}
	}
	
	/**
	 * 게시판 삭제 처리
	 * @param boardModel
	 */
	public void delete(BoardModel boardModel) {
		Session session = this.sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			//간단하다. delete 메소드 실행(boardModel에는 num 값이 들어가있어야 함)
			//board.xml 에서 <id/> 와 num 값이 매핑되어 삭제
			session.delete(boardModel);
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			e.printStackTrace();
		} finally {
			if (session != null) session.close();
		}
	}
	
}
