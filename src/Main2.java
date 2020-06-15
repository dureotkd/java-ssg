//- member join
//- member login
//- member logout
//- member whoami
//- article wirte
//- article modify  
//- article delete 1
//- article list 1
//- article list 2 
//- article boardList
//- article changeBoard 1
//- article detail3 
//- article list 1 안녕 
//- article createBoard 
//- article deleteBoardNotice
//
//// 명령어 리스트 [미완료]
//- build site 
//- build startAutoSite 
//- build stopAutoSite*/

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class Main {
	public static void main(String[] args) {
		
		App app = new App();
		app.start();
	}
}

// Session
// 현재 사용자가 이용중인 정보
// 이 안의 정보는 사용자가 프로그램을 사용할 때 동안은 계속 유지된다.
class Session {
	private Member loginedMember;
	private Board currentBoard;

	public Member getLoginedMember() {
		return loginedMember;
	}

	public void setLoginedMember(Member loginedMember) {
		this.loginedMember = loginedMember;
	}

	public Board getCurrentBoard() {
		return currentBoard;
	}

	public void setCurrentBoard(Board currentBoard) {
		this.currentBoard = currentBoard;
	}

	public boolean isLogined() {
		return loginedMember != null;
	}
}

// Factory
// 프로그램 전체에서 공유되는 객체 리모콘을 보관하는 클래스

class Factory {
	private static Session session;
	private static DB db;
	private static BuildService buildService;
	private static ArticleService articleService;
	private static ArticleDao articleDao;
	private static MemberService memberService;
	private static MemberDao memberDao;
	private static Scanner scanner;

	public static Session getSession() {
		if (session == null) {
			session = new Session();
		}

		return session;
	}

	public static Scanner getScanner() {
		if (scanner == null) {
			scanner = new Scanner(System.in);
		}

		return scanner;
	}

	public static DB getDB() {
		if (db == null) {
			db = new DB();
		}
		


		return db;
	}

	public static ArticleService getArticleService() {
		if (articleService == null) {
			articleService = new ArticleService();
		}

		return articleService;
	}

	public static ArticleDao getArticleDao() {
		if (articleDao == null) {
			articleDao = new ArticleDao();
		}

		return articleDao;
	}

	public static MemberService getMemberService() {
		if (memberService == null) {
			memberService = new MemberService();
		}
		return memberService;
	}

	public static MemberDao getMemberDao() {
		if (memberDao == null) {
			memberDao = new MemberDao();
		}

		return memberDao;
	}

	public static BuildService getBuildService() {
		if (buildService == null) {
			buildService = new BuildService();
		}

		return buildService;
	}
}

// App
class App {
	private Map<String, Controller> controllers;

	// 컨트롤러 만들고 한곳에 정리
	// 나중에 컨트롤러 이름으로 쉽게 찾아쓸 수 있게 하려고 Map 사용
	void initControllers() {
		controllers = new HashMap<>();
		controllers.put("build", new BuildController());
		controllers.put("article", new ArticleController());
		controllers.put("member", new MemberController());
	}

	public App() {
		// 컨트롤러 등록
		initControllers();

		// 관리자 회원 생성
		Factory.getMemberService().join("admin", "admin", "관리자");

		// 공지사항 게시판 생성
		Factory.getArticleService().makeBoard("공지사항", "notice");
		// 자유 게시판 생성
		Factory.getArticleService().makeBoard("자유게시판", "free");

		// 현재 게시판을 1번 게시판으로 선택
		Factory.getSession().setCurrentBoard(Factory.getArticleService().getBoard(1));

	}

	public void start() {

		while (true) {
			System.out.printf("명령어 : ");
			String command = Factory.getScanner().nextLine().trim();

			if (command.length() == 0) {
				continue;
			} else if (command.equals("exit")) {
				break;
			}

			Request reqeust = new Request(command);

			if (reqeust.isValidRequest() == false) {
				continue;
			}

			if (controllers.containsKey(reqeust.getControllerName()) == false) {
				continue;
			}

			controllers.get(reqeust.getControllerName()).doAction(reqeust);
		}
		Factory.getScanner().close();
	}
}

// Request
class Request {
	private String requestStr;
	private String controllerName;
	private String actionName;
	private String arg1;
	private String arg2;
	private String arg3;

	boolean isValidRequest() {
		return actionName != null;
	}

	Request(String requestStr) {
		this.requestStr = requestStr;
		String[] requestStrBits = requestStr.split(" ");
		this.controllerName = requestStrBits[0];

		if (requestStrBits.length > 1) {
			this.actionName = requestStrBits[1];
		}

		if (requestStrBits.length > 2) {
			this.arg1 = requestStrBits[2];
		}

		if (requestStrBits.length > 3) {
			this.arg2 = requestStrBits[3];
		}

		if (requestStrBits.length > 4) {
			this.arg3 = requestStrBits[4];
		}
	}

	public String getControllerName() {
		return controllerName;
	}

	public void setControllerName(String controllerName) {
		this.controllerName = controllerName;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getArg1() {
		return arg1;
	}

	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}

	public String getArg2() {
		return arg2;
	}

	public void setArg2(String arg2) {
		this.arg2 = arg2;
	}

	public String getArg3() {
		return arg3;
	}

	public void setArg3(String arg3) {
		this.arg3 = arg3;
	}
}

// Controller
abstract class Controller {
	abstract void doAction(Request reqeust);
}

class ArticleController extends Controller {
	private ArticleService articleService;
	private Article article;
	
	ArticleController() {
		articleService = Factory.getArticleService();
	}

	public void doAction(Request reqeust) {
		if (reqeust.getActionName().equals("list")) {
			actionList(reqeust);
		} else if (reqeust.getActionName().equals("write")) {
			actionWrite(reqeust);
		} else if (reqeust.getActionName().equals("boardList")) {
			actionBoardList(reqeust);
		} else if (reqeust.getActionName().equals("changeBoard")) {
			actionChangeBoardfree(reqeust);
		}  else if (reqeust.getActionName().equals("modify")) {
			actionModify(reqeust);
		} else if (reqeust.getActionName().equals("delete")) {
			actionDelete(reqeust);
		} else if (reqeust.getActionName().equals("detail")) {
			int articleId = Factory.getScanner().nextInt();
			Factory.getScanner().nextLine();
			actiondetail(reqeust, articleId);
		}else if (reqeust.getActionName().equals("listSearch")){
			actionlistSearch(reqeust);
		} else if (reqeust.getActionName().equals("createBoard")) {
			actionCreateBoard(reqeust);
		} else if (reqeust.getActionName().equals("deleteBoard")) {
			actionDeleteBoard(reqeust);
		} else {
			System.out.printf("알 수 없는 명령어\n");
		}
	}
	



	private void actionDeleteBoard(Request reqeust) {
		List<Board> boards = articleService.getBoards();
		
		Member loginedMember = Factory.getSession().getLoginedMember();
		
			if ( loginedMember == null ) {
				System.out.println("이용 불가");
				return;
			}
			if (loginedMember.getLoginId().equals("admin") ) {
				for ( Board board : boards ) {
					System.out.printf("[%d]번 %s\n",board.getId(),board.getName() );
				}
				System.out.print("삭제하실 게시판 번호를 입력해주세요 : ");
				int boardId = Factory.getScanner().nextInt();
				for ( Board board : boards ) {
				if ( boardId == board.getId()) {
						Factory.getArticleService().boardDelete(boardId);
						
						System.out.println("삭제되었습니다.");
					}
				
				
				}
				
			}
				
		}

	

	private void actionCreateBoard(Request reqeust) {
		
		Member loginedMember = Factory.getSession().getLoginedMember();
		if ( loginedMember == null ) {
			System.out.println("이용불가");
			return;
		}
		else if (loginedMember.getLoginId().equals("admin") ) {
			System.out.print("게시판 이름 : ");
			String boardName = Factory.getScanner().nextLine().trim();
			System.out.print("게시판  상세 : ");
			String boardCode = Factory.getScanner().nextLine().trim();
			Factory.getArticleService().makeBoard(boardName,boardCode);
			System.out.println("게시판이 성공적으로 생성되었습니다.");
		}
		else {
			System.out.println("이용불가");
			return;
		}
	
	}

	private void actionlistSearch(Request reqeust) {
		List<Board> boards = articleService.getBoards();
		List<Article> articles = articleService.getArticles();
		
		for ( Board board : boards ) {
			System.out.printf("[%d] %s \n",board.getId(),board.getName());
			
		}
		System.out.println("");
		System.out.print("원하는 게시물이 있는 게시판 번호를 입력해주세요 : ");
		System.out.println("");
        int boardId = Factory.getScanner().nextInt();
        System.out.println("");
        Factory.getScanner().nextLine();

        int page = 1;
        int articleSize = articles.size();
        int lastPage = 0;
        int lastLimit = 1;
        int limit = 10;
        
        
        if(articleSize % limit == 0){ //  articleSize(10) % limit == 0 
           lastPage = articleSize / limit; // articleSize를 limit 으로  나눳을떄  lastPage == 1
        }else{
           lastPage = (articleSize / limit) + 1; // articleSize를 limit으로 나누고 +1 -> 2페이지 11부터시작 
        }
       
        while(true){
        	System.out.println("====  Exit  : return [input] ====");
            System.out.println("==== Search : 검색    [input] ====");
            
            System.out.println("");
            for ( Board board : boards ) {
            	if ( board.getId() == boardId ) {
            	System.out.printf("==== [%s] 리스트 ====\n",board.getName());
            	}
  
            }
            System.out.println("");
            
            for(int i=(limit - 10);i<limit;i++){ 
            	if ( articleSize > i )
            	{
               article = articles.get(i);
               if (article.getBoardId() == boardId) {
            	  System.out.printf("%d번 게시물\n", article.getId());
                  System.out.printf("제목 : %s\n", article.getTitle());
               		}
            	}
            }
            if ( lastPage == page && lastPage != 1 ) {
            	System.out.println("=== Last Page ===");
            	System.out.println("       << "+ page + "    ");
               } 
            if ( lastPage > page && page != 1 ) {
            	System.out.println( " << == "+ page + " == >>");
            }
            if ( page == 1 ) {
            	System.out.println("=== First Page ===");
            	System.out.println("        "+ page + " >>   ");
            }
           
            // >> 할려?
            String pageUp = Factory.getScanner().nextLine().trim(); // 원하는 스캐너 입력 받고
          
            if ( pageUp.equals(">") && lastPage > page) {
               page++;// 페이지는 1 올라가고 >>할때마다
               limit+=10; // 0번째 -> 1~10 번호 출력 // 1번 -> 11 ~ 20 // 11 ~ 20 번호 출력  // 2번 -> 21~30 번호 출력 -->  31~20 번호 출력  20 ~ 10 번호 출력 10~1 번호 출력
               System.out.println("다음페이지로 이동합니다.\n");
              
            }     
               else if(pageUp.equals("<")&& page>1){
               System.out.println("이전페이지로 이동합니다. \n");
               page--; // <<면 페이지 다운
               limit-=10; // limit -10씩 
               
            }  
            
               else if(pageUp.equals("<<")&& page>1) {
            	   System.out.println("첫페이지로 이동합니다.\n");
            	   page = 1;
            	   limit = 10;
               }
            	
               else if(pageUp.equals(">>")) {
            	   System.out.println("마지막페이지로 이동합니다.\n");
            	   page = lastPage;
            	   limit = lastPage*10; 
               }
 
               else if(pageUp.equals("return")) {
               break;
            }  
            
            if ( pageUp.equals("검색")) {
            System.out.println("\n\n");
            System.out.print("관련 검색어 불러오기 : ");
    		String serchTitle = Factory.getScanner().nextLine().trim();
    		
    		System.out.println("\n");
    		for ( Article article : articles ) {
    			if ( article.getTitle().equals(serchTitle) == false && article.getBody().equals(serchTitle) == false ) {
    				System.out.printf("해당 [ %s ]가 포함된 게시글이 존재하지 않습니다 .\n",serchTitle);
    				
    				System.out.printf("\n\n");
    			}
    			break;
    		}
    		
    		for ( Article article : articles) {
    			if ( article.getTitle().equals(serchTitle) || article.getBody().equals(serchTitle)) {
    				System.out.printf("=====[%d]번 게시글====\n",article.getId());
    				System.out.printf("제목 : %s\n",article.getTitle());
    				System.out.printf("내용 : %s\n",article.getBody());
    				System.out.println("====================");
    				System.out.println("\n\n");    				
    						}
    				continue;
    					}
            		}
         		}
        	}
  	
        /// 전체 article 소환해서 관련검색어로 serach 해가꼬 가저오기.


	private void actiondetail(Request reqeust, int articleId) {
		List<Article> articles = articleService.getArticles();
		
		for (Article article : articles) {
			if (article.getId() == articleId) {
				System.out.println(article.toString());
			}			
		}
	}

	private void actionDelete(Request reqeust) {
		Member loginedMember = Factory.getSession().getLoginedMember();
		List<Article> articles = articleService.getArticles();
		
		if (loginedMember != null) {
			int memberId = Factory.getSession().getLoginedMember().getId();
			for (Article article : articles) {
				if (article.getMemberId() == memberId) {
					
					System.out.printf("게시물 번호: %d\n", article.getId());
					System.out.printf("내용 : %s\n", article.getTitle());
					System.out.printf("제목 : %s\n", article.getBody());
					System.out.printf("=============================\n");
				}
			}
			
			System.out.print("삭제할 게시물 번호를 입력하세요:");
			int articleId = Factory.getScanner().nextInt();
			Factory.getScanner().nextLine();
			for (Article article : articles) {
				if (article.getId() == articleId) {
					articleService.articleDelete(articleId);
					
					System.out.println("게시물이 삭제되었습니다.");
					break;
				}
			} 
			
			
			
			
		} else {
			System.out.println("로그인먼저 해주세요.");
		}
	}

	private void actionModify(Request reqeust) {
		List<Article> articles = articleService.getArticles();
		Member loginedMember = Factory.getSession().getLoginedMember();

		if (loginedMember != null) {
			int memberId = Factory.getSession().getLoginedMember().getId();
			for (Article article : articles) {
				if (article.getMemberId() == memberId) {
					System.out.printf("게시물 번호: %d\n", article.getId());
					System.out.printf("내용 : %s\n", article.getTitle());
					System.out.printf("제목 : %s\n", article.getBody());
					System.out.printf("=============================\n");
				}
			}
			System.out.print("수정할 게시물 번호를 입력해주세요 : ");
			int articleId = Factory.getScanner().nextInt();
			Factory.getScanner().nextLine();
			
			article = articleService.getArticleByNum(articleId);
			System.out.printf("수정할 제목 : ");
			String title = Factory.getScanner().nextLine();
			System.out.printf("수정할 내용 : ");
			String body = Factory.getScanner().nextLine();
			
			if (article.getMemberId() == loginedMember.getId()) {
					article.setTitle(title);
					article.setBody(body);
					articleService.articlemodify(article);
			} else {
				System.out.println("해당 게시물이 없습니다.");
			}

			// 현재 게시판 id 가져오기

		} else {
			System.out.println("로그인먼저 해주세요.");
		}

	}

	// 게시판 변경
	private void actionChangeBoardfree(Request reqeust) {
		List<Article> articles = articleService.getArticles();
		List<Board> boards = articleService.getBoards();
		

		for (Board board : boards) {
			System.out.printf("[%d] %s\n", board.getId(), board.getName());
		}
		
		
		System.out.print("이동하고 싶은 게시판 번호를 입력해주세요. : ");
		int boardId = Factory.getScanner().nextInt();
		Factory.getScanner().nextLine();
		
		for (Board board : boards) {
			if ( boardId == board.getId() ) {
				Factory.getSession().setCurrentBoard(Factory.getArticleService().getBoard(boardId));
				Board currentBoard = Factory.getSession().getCurrentBoard();
				System.out.printf("%s으로 변경되었습니다.\n", currentBoard.getName());
				
				return;
			}
		}
		
	}

	// 게시판 리스트
	private void actionBoardList(Request reqeust) {
		List<Board> boards = articleService.getBoards();

		for (Board board : boards) {
			System.out.printf("[%d] %s\n", board.getId(), board.getName());
		}
	}
	
	// 게시물 리스트
	 private void actionList(Request reqeust) {
		    
	        List<Article> articles = articleService.getArticles();
	        List<Board> boards = articleService.getBoards();
	        for (Board board : boards) {
	        System.out.printf("[%d] %s\n", board.getId(), board.getName());
	        }
	   
	        System.out.print("원하는 게시물이 있는 게시판 번호를 입력해주세요 : ");
	        int boardId = Factory.getScanner().nextInt();
	        Factory.getScanner().nextLine();
	        
	      
	        int page = 1;
	        int id = 0;
	        int articleSize = articles.size();
	        int lastPage = 0;
	        int limitSum = 0;
	        int lastLimit = 1;
	        int limit = 10;
	        if(articleSize % limit == 0){
	           lastPage = articleSize / limit; // articleSize를 limit 으로  나눳을떄  1페이지 1부터 10까지
	        }else{
	           lastPage = (articleSize / limit) + 1; // articleSize를 limit으로 나누고 +1 -> 2페이지 11부터시작 
	        }
	       
	        while(true){
	        	
	        	System.out.println("==== Exit : return [ 입력해주세요 ]  ====");
 	
	            for(int i=(limit - 10);i<limit;i++){ 
	            	if ( articleSize > i )
	            	{
	               article = articles.get(i);
	               if (article.getBoardId() == boardId) {	// 입력한 게시판 번호랑 같은지1입력시  1공지사항 인지
	            	  System.out.printf("%d번 게시물\n", article.getId());
	                  System.out.printf("제목 : %s\n", article.getTitle());
	               		}
	            	}
	            }
	            
	            if ( lastPage == page && lastPage != 1 ) {
	            	System.out.println("=== Last Page ===");
	            	System.out.println("       << "+ page + "    ");
	               } 
	            if ( lastPage > page && page != 1 ) {
	            	System.out.println( " << == "+ page + " == >>");
	            }
	            if ( page == 1 ) {
	            	System.out.println("=== First Page ===");
	            	System.out.println("        "+ page + " >>   ");
	            }

	            String pageUp = Factory.getScanner().nextLine().trim(); // 원하는 스캐너 입력 받고
	            if ( pageUp.equals(">") && lastPage > page) {
	               page++;// 페이지는 1 올라가고 >>할때마다
	               limit+=10; // 0번째 -> 1~10 번호 출력 // 1번 -> 11 ~ 20 // 11 ~ 20 번호 출력  // 2번 -> 21~30 번호 출력 -->  31~20 번호 출력  20 ~ 10 번호 출력 10~1 번호 출력
	               System.out.println("다음페이지로 이동합니다.\n");
	              
	            }     
	               else if(pageUp.equals("<")&& page>1){
	               System.out.println("이전페이지로 이동합니다. \n");
	               page--; // <<면 페이지 다운
	               limit-=10; // limit -10씩 
	               
	            }  
	            
	               else if(pageUp.equals("<<")&& page>1) {
	            	   System.out.println("첫페이지로 이동합니다.\n");
	            	   page = 1;
	            	   limit = 10;
	               }
	            	
	               else if(pageUp.equals(">>")) {
	            	   System.out.println("마지막페이지로 이동합니다.\n");
	            	   page = lastPage;
	            	   limit = lastPage*10; 
	               }

	               else if(pageUp.equals("return")) {
	               break;
	            }    
	         }
	     }

	private void actionWrite(Request reqeust) {
		List<Board> boards = articleService.getBoards();
		Member loginedMember = Factory.getSession().getLoginedMember();
		if (loginedMember != null) {
			System.out.println();
			System.out.printf("제목 : ");
			String title = Factory.getScanner().nextLine();
			System.out.printf("내용 : ");
			String body = Factory.getScanner().nextLine();

			// 현재 게시판 id 가져오기
			int boardId = Factory.getSession().getCurrentBoard().getId();
			//  현재 게시판 안에 게시글 고유번호 가저오기
			
			// 현재 로그인한 회원의 id 가져오기
			int memberId = Factory.getSession().getLoginedMember().getId();
			
			int newId = articleService.write(boardId, memberId, title, body);


			System.out.printf("[TOTAL] %d번  [%s] %d번글이 생성되었습니다.\n", newId ,Factory.getSession().getCurrentBoard().getName(),newId);
		} else {
			System.out.println("글 작성하기 전에 로그인먼저 해주세요.");
		}
	}
}

class BuildController extends Controller {
	private BuildService buildService;

	BuildController() {
		buildService = Factory.getBuildService();
	}

	@Override
	void doAction(Request reqeust) {
		if (reqeust.getActionName().equals("site")) {
			System.out.println("==== 게시글 자동생성 중.... ====");
			actionSite(reqeust);
		}
		
		if (reqeust.getActionName().equals("stop")) {
			System.out.println("==== 게시글 자동생성 종료.... ====");
			actionStop(reqeust);
		}
	}
	
	
	
	

	private void actionStop(Request reqeust) {
		buildService.buildStop();
	}

	private void actionSite(Request reqeust) {
		buildService.buildSite();
	}
}

class MemberController extends Controller {
	private MemberService memberService;

	MemberController() {
		memberService = Factory.getMemberService();
	}

	void doAction(Request reqeust) {
		if (reqeust.getActionName().equals("logout")) {
			actionLogout(reqeust);
		} else if (reqeust.getActionName().equals("login")) {
			actionLogin(reqeust);
		} else if (reqeust.getActionName().equals("whoami")) {
			actionWhoami(reqeust);
		} else if (reqeust.getActionName().equals("join")) {
			actionJoin(reqeust);
		}
	}

	private void actionJoin(Request reqeust) {
		System.out.println("\n==회원가입 시작==");
		
		
		String name;
		String loginId;
		String loginPw;
		String loginPwConfirm;

		while (true) {
			System.out.print("이름 : ");
			name = Factory.getScanner().nextLine().trim();

			if (name.length() == 0) {
				System.out.println("이름을 입력해주세요.");
				continue;
			}

			if (name.length() < 2) {
				System.out.println("이름을 2글자 이상 입력해주세요.");
				continue;
			}
			break;
		}

		while (true) {
			System.out.print("아이디 : ");
			loginId = Factory.getScanner().nextLine().trim();

			if (loginId.length() == 0) {
				System.out.println("아이디를 입력해주세요.");
				continue;
			}

			if (loginId.length() < 2) {
				System.out.println("아이디를 2글자 이상 입력해주세요.");
				continue;
			}
			if (memberService.isUsedLoginId(loginId)) {
				System.out.printf("==입력하신 아이디(%s)는 이미 사용중입니다==\n", loginId);
				continue;
			}
			break;
		}
		while (true) {
			// 비밀번호, 비밀번호 확인 일치 불일치 알려줌
			boolean loginPwValid = true;

			while (true) {
				System.out.print("비밀번호 : ");
				loginPw = Factory.getScanner().nextLine().trim();

				if (loginPw.length() == 0) {
					System.out.println("비밀번호를 입력해주세요.");
					continue;
				}

				if (loginPw.length() < 2) {
					System.out.println("비밀번호를 2글자 이상 입력해주세요.");
					continue;
				}
				break;
			}
			while (true) {
				System.out.print("비밀번호 확인 : ");
				loginPwConfirm = Factory.getScanner().nextLine().trim();

				if (loginPwConfirm.length() == 0) {
					System.out.println("비밀번호를 입력해주세요.");
					continue;
				}

				if (loginPw.equals(loginPwConfirm) == false) {
					System.out.println("비밀번호확인이 일치하지 않습니다.\n");
					loginPwValid = false;
					break;
				}
				break;
			}

			// true일때 반복분 빠져나감
			if (loginPwValid) {
				break;
			}

		}

		// 로그인 아이디 중복 여부를 알려줌
		int rs = memberService.join(loginId, loginPw, name);

		if (rs == 1) {
			System.out.println("성공하였습니다.");
		} else if (rs == -1) {
			System.out.println("입력하신 아이디는 이미 사용중입니다.");
		}
		System.out.println("==회원가입 끝==\n");
	}

	private void actionWhoami(Request reqeust) {
		Member loginedMember = Factory.getSession().getLoginedMember();

		if (loginedMember == null) {
			System.out.println("로그인되어있지 않습니다.");
		} else {
			System.out.println(loginedMember.getName());
		}

	}

	private void actionLogin(Request reqeust) {
		Member loginedMember = Factory.getSession().getLoginedMember();

		if (loginedMember != null) {
			System.out.println("로그아웃먼저 해주세요");
		} else {
			System.out.printf("로그인 아이디 : ");
			String loginId = Factory.getScanner().nextLine().trim();

			System.out.printf("로그인 비번 : ");
			String loginPw = Factory.getScanner().nextLine().trim();

			Member member = memberService.getMemberByLoginIdAndLoginPw(loginId, loginPw);

			if (member == null) {
				System.out.println("일치하는 회원이 없습니다.");
			} else {
				System.out.println(member.getName() + "님 환영합니다.");
				Factory.getSession().setLoginedMember(member);
			}
		}

	}

	private void actionLogout(Request reqeust) {
		Member loginedMember = Factory.getSession().getLoginedMember();

		if (loginedMember != null) {
			Session session = Factory.getSession();
			System.out.println("로그아웃 되었습니다.");
			session.setLoginedMember(null);
		}

	}
}

//Service
class BuildService {
	private static boolean buildSite;
	
	static {
		buildSite = false;
	}
	ArticleService articleService;
	MemberService memberService;

	BuildService() {
		articleService = Factory.getArticleService();
		memberService = Factory.getMemberService();
	}
	
	public void buildStop() {
		buildSite = false;
	}

	void buildSite() {
		buildSite = true;
		
		new Thread(() -> {
			while ( buildSite ) {
				Util.makeDir("site_template/articleDetail");
				
				String head = Util.getFileContents("site_template/part/head.html");
				String foot = Util.getFileContents("site_template/part/foot.html");

				// 각 게시판 별 게시물리스트 페이지 생성
				List<Board> boards = articleService.getBoards();
				List<Member> members = memberService.getMembers();
				String menu = "";
				for (Board board : boards) {
					menu += "<li><a href=\"../article/" + board.getCode() + "-list-1.html\">"+ board.getName() + "</a></li>";
				}
				head = head.replace("{$boardMenu}", menu);
				Util.writeFileContents("site_template/part/index.html", head);
				
				for (Board board : boards) {
					String fileName = board.getCode() + "-list-1.html";

					String html = "";

					List<Article> articles = articleService.getArticlesByBoardCode(board.getCode());
					
					String template = Util.getFileContents("site_template/article/list.html");
					int i = 1;
					for (Article article : articles) {
						for(Member member : members) {
							if(article.getMemberId() == (member.getId())) {
								html += "<tr>";
								html += "<td>" + i + "</td>";
								html += "<td><a href=\"../articleDetail/" + article.getId() + ".html\">" + article.getTitle() + "</a></td>";
								html += "<td>" + member.getLoginId() + "</td>";
								html += "<td>" + article.getRegDate() + "</td>";
								html += "</tr>";
							}
						}
						
						i++;
						
					
					}
					
					html = template.replace("${TR}", html);

					html = head + html + foot;

					Util.writeFileContents("site_template/article/" + fileName, html);
				}
				
				//게시판 생성
				
				// 게시물 별 파일 생성
				List<Article> articles = articleService.getArticles();

				for (Article article : articles) {
					String html = "<html>";

					html += "<head>";
					html += "<meta charset=\"utf-8\">";
					html += "<link rel=\"stylesheet\" href=\"../css/Style.css\">";
					html += "</head>";

					html += "<body>";
					html += "<section class=\"detail-line\">";
					html += "</section>";
					html += "<div class=\"title\">제목 : " + article.getTitle() + "</div>";
					html += "<section class=\"detail-line2\">";
					html += "</section>";
					html += "<div class=\"detail\">" + article.getBody() + "</div>";
					html += "<div class=\"before\"><a href=\"" + (article.getId() - 1) + ".html\"><< 이전글</a></div>";
					html += "<div class=\"next\"><a href=\"" + (article.getId() + 1) + ".html\">다음글 >></a></div>";
					html += "</body>";

					html += "</html>";
					html = head + html + foot;
					Util.writeFileContents("site_template/articleDetail/" + article.getId() + ".html", html);
				}
				}
				
		}).start();
	}

	
}
class ArticleService {
	private ArticleDao articleDao;

	ArticleService() {
		articleDao = Factory.getArticleDao();
	}

	
	public Object boardDelete(int boardId) {
		return articleDao.boardDelete(boardId);
	}
	

	public List<Article> getArticlesByBoardCode(String code) {
		return articleDao.getArticlesByBoardCode(code);
	}

	public int articlemodify(Article article) {
		return articleDao.save(article);
	}

	public Article getArticleByNum(int articleId) {
		return articleDao.getArticleByNum(articleId);
	}

	public boolean articleDelete(int articleId) {
		return articleDao.articleDelete(articleId);

	}

	public Board getBoardBycode(String code) {
		return articleDao.getBoardByCode(code);
	}

	public List<Board> getBoards() {
		return articleDao.getBoards();
	}

	public int makeBoard(String name, String code) {
		Board oldBoard = articleDao.getBoardByCode(code);

		if (oldBoard != null) {
			return -1;
		}

		Board board = new Board(name, code);
		return articleDao.saveBoard(board);
	}

	public Board getBoard(int id) {
		return articleDao.getBoard(id);
	}

	public int write(int boardId, int memberId, String title, String body) {
		Article article = new Article(boardId, memberId, title, body);
		return articleDao.save(article);
	}

	public List<Article> getArticles() {
		return articleDao.getArticles();
	}
}

class MemberService {
	private MemberDao memberDao;

	MemberService() {
		memberDao = Factory.getMemberDao();
	}

	public List<Member> getMembers() {
		return memberDao.getMembers();
	}

	public boolean isUsedLoginId(String loginId) {
		Member member = memberDao.getMemberByLoginId(loginId);

		if (member == null) {
			return false;
		}
		return true;
	}

	public Member getMemberByLoginIdAndLoginPw(String loginId, String loginPw) {
		return memberDao.getMemberByLoginIdAndLoginPw(loginId, loginPw);
	}

	public int join(String loginId, String loginPw, String name) {
		Member oldMember = memberDao.getMemberByLoginId(loginId);

		if (oldMember != null) {
			return -1;
		}

		Member member = new Member(loginId, loginPw, name);
		return memberDao.save(member);
	}

	public Member getMember(int id) {
		return memberDao.getMember(id);
	}
}

// Dao
class ArticleDao {
	DB db;

	ArticleDao() {
		db = Factory.getDB();
	}

	
	public Object boardDelete(int boardId) {
		return db.boardDelete(boardId);
	}

	public List<Article> getArticlesByBoardCode(String code) {
		return db.getArticlesByBoardCode(code);
	}


	public Article getArticleByNum(int articleId) {
		return db.getArticleByNum(articleId);
	}

	public boolean articleDelete(int articleId) {
		return db.articleDelete(articleId);
	}

	public List<Board> getBoards() {
		return db.getBoards();
	}

	public Board getBoardByCode(String code) {
		return db.getBoardByCode(code);
	}

	public int saveBoard(Board board) {
		return db.saveBoard(board);
	}

	public int save(Article article) {
		return db.saveArticle(article);
	}

	public Board getBoard(int id) {
		return db.getBoard(id);
	}

	public List<Article> getArticles() {
		return db.getArticles();
	}

}

class MemberDao {
	DB db;

	MemberDao() {
		db = Factory.getDB();
	}

	public List<Member> getMembers() {
		return db.getMembers();
	}

	public Member getMemberByLoginIdAndLoginPw(String loginId, String loginPw) {
		return db.getMemberByLoginIdAndLoginPw(loginId, loginPw);
	}

	public Member getMemberByLoginId(String loginId) {
		return db.getMemberByLoginId(loginId);
	}

	public Member getMember(int id) {
		return db.getMember(id);
	}

	public int save(Member member) {
		return db.saveMember(member);
	}
}

// DB
class DB {
	private Map<String, Table> tables;

	public DB() {
		String dbDirPath = getDirPath();
		Util.makeDir(dbDirPath);

		tables = new HashMap<>();

		tables.put("article", new Table<Article>(Article.class, dbDirPath));
		tables.put("board", new Table<Board>(Board.class, dbDirPath));
		tables.put("member", new Table<Member>(Member.class, dbDirPath));
	}



	public boolean boardDelete(int boardId) {
		List<Board> boards = getBoards();
		String filePath = "db/board/"+boardId+".json";
		File f = new File(filePath);
		for (Board board : boards) {
			if (isFileExists(filePath)) {
				if (f.delete()) {
					boards.remove(board);
					return true;
				}
			}
		}
		return false;
	}
	

	public List<Article> getArticlesByBoardCode(String code) {
		Board board = getBoardByCode(code);
		// free => 2
		// notice => 1

		List<Article> articles = getArticles();
		List<Article> newArticles = new ArrayList<>();

		for (Article article : articles) {
			if (article.getBoardId() == board.getId()) {
				newArticles.add(article);
			}
		}

		return newArticles;
	}

	public Article getArticleByNum(int articleId) {
		List<Article> articles = getArticles();
		
		for(Article article : articles) {
			if(article.getId() == articleId) {
				return article;
			}
		}
		return null;
	}

	boolean isFileExists(String filePath) {
		File f = new File(filePath);
		if (f.isFile()) {
			return true;
		}

		return false;
	}

	public boolean articleDelete(int articleId) {
		List<Article> articles = getArticles();
		String filePath = "db/article/" + articleId + ".json";
		File f = new File(filePath);
		for (Article article : articles) {
			if (isFileExists(filePath)) {
				if (f.delete()) {
					articles.remove(article);

					return true;

				}
			}
		}
		return false;
	}

	public Member getMemberByLoginIdAndLoginPw(String loginId, String loginPw) {
		List<Member> members = getMembers();

		for (Member member : members) {
			if (member.getLoginId().equals(loginId) && member.getLoginPw().equals(loginPw)) {
				return member;
			}
		}

		return null;
	}

	public Member getMemberByLoginId(String loginId) {
		List<Member> members = getMembers();

		for (Member member : members) {
			if (member.getLoginId().equals(loginId)) {
				return member;
			}
		}

		return null;
	}

	public List<Member> getMembers() {
		return tables.get("member").getRows();
	}

	public Board getBoardByCode(String code) {
		List<Board> boards = getBoards();

		for (Board board : boards) {
			if (board.getCode().equals(code)) {
				return board;
			}
		}

		return null;
	}

	public List<Board> getBoards() {
		return tables.get("board").getRows();
	}

	public Member getMember(int id) {
		return (Member) tables.get("member").getRow(id);
	}

	public int saveBoard(Board board) {
		return tables.get("board").saveRow(board);
	}

	public String getDirPath() {
		return "db";
	}

	public int saveMember(Member member) {
		return tables.get("member").saveRow(member);
	}

	public Board getBoard(int id) {
		return (Board) tables.get("board").getRow(id);
	}

	public List<Article> getArticles() {
		return tables.get("article").getRows();
	}

	public int saveArticle(Article article) {
		return tables.get("article").saveRow(article);
	}

	public void backup() {
		for (String tableName : tables.keySet()) {
			Table table = tables.get(tableName);
			table.backup();
		}
	}
}

// Table
class Table<T> {
	private Class<T> dataCls;
	private String tableName;
	private String tableDirPath;

	public Table(Class<T> dataCls, String dbDirPath) {
		this.dataCls = dataCls;
		this.tableName = Util.lcfirst(dataCls.getCanonicalName());
		this.tableDirPath = dbDirPath + "/" + this.tableName;

		Util.makeDir(tableDirPath);
	}

	private String getTableName() {
		return tableName;
	}

	public int saveRow(T data) {
		Dto dto = (Dto) data;

		if (dto.getId() == 0) {
			int lastId = getLastId();
			int newId = lastId + 1;
			dto.setId(newId);
			setLastId(newId);
		}

		String rowFilePath = getRowFilePath(dto.getId());

		Util.writeJsonFile(rowFilePath, data);

		return dto.getId();
	};

	private String getRowFilePath(int id) {
		return tableDirPath + "/" + id + ".json";
	}

	private void setLastId(int lastId) {
		String filePath = getLastIdFilePath();
		Util.writeFileContents(filePath, lastId);
	}

	private int getLastId() {
		String filePath = getLastIdFilePath();

		if (Util.isFileExists(filePath) == false) {
			int lastId = 0;
			Util.writeFileContents(filePath, lastId);
			return lastId;
		}

		return Integer.parseInt(Util.getFileContents(filePath));
	}

	private String getLastIdFilePath() {
		return this.tableDirPath + "/lastId.txt";
	}

	public T getRow(int id) {
		return (T) Util.getObjectFromJson(getRowFilePath(id), dataCls);
	}

	public void backup() {

	}

	void delete(int id) {

	}

	List<T> getRows() {
		int lastId = getLastId();

		List<T> rows = new ArrayList<>();

		for (int id = 1; id <= lastId; id++) {
			T row = getRow(id);

			if (row != null) {
				rows.add(row);
			}
		}

		return rows;
	};
}

// DTO
abstract class Dto {
	private int id;
	private int boardArticleId;
	
	public int getBoardArticleId() {
		return boardArticleId;
	}

	public void setBoardArticleId(int boardArticleId) {
		this.boardArticleId = boardArticleId;
	}

	private String regDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRegDate() {
		return regDate;
	}

	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}

	Dto() {
		this(0);
	}

	Dto(int id) {
		this(id, Util.getNowDateStr());
	}

	Dto(int id, String regDate) {
		this.id = id;
		this.boardArticleId = boardArticleId;
		this.regDate = regDate;
	}
}

class Board extends Dto {
	private String name;
	private String code;

	public Board() {
	}

	public Board(String name, String code) {
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}

class Article extends Dto {
	private int boardId;
	private int memberId;
	private String title;
	private String body;

	public Article() {

	}

	public Article(int boardId, int memberId, String title, String body) {
		this.boardId = boardId;
		this.memberId = memberId;
		this.title = title;
		this.body = body;
	}

	public int getBoardId() {
		return boardId;
	}

	public void setBoardId(int boardId) {
		this.boardId = boardId;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return " [제목 : " + title + "]" + "," + "[내용 : " + body + "]";
	}
	
	
}

class ArticleReply extends Dto {
	private int id;
	private String regDate;
	private int articleId;
	private int memberId;
	private String body;
	private String reply;
	
	public String getReply() {
		return reply;
	}

	public void setReply(String reply) {
		this.reply = reply;
	}

	ArticleReply() {
		
	}

	public int getArticleId() {
		return articleId;
	}

	public void setArticleId(int articleId) {
		this.articleId = articleId;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

}

class Member extends Dto {
	private String loginId;
	private String loginPw;
	private String name;

	public Member() {

	}

	public Member(String loginId, String loginPw, String name) {
		this.loginId = loginId;
		this.loginPw = loginPw;
		this.name = name;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public String getLoginPw() {
		return loginPw;
	}

	public void setLoginPw(String loginPw) {
		this.loginPw = loginPw;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

// Util
class Util {
	// 현재날짜문장
	public static String getNowDateStr() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat Date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateStr = Date.format(cal.getTime());
		return dateStr;
	}

	// 파일에 내용쓰기
	public static void writeFileContents(String filePath, int data) {
		writeFileContents(filePath, data + "");
	}

	// 첫 문자 소문자화
	public static String lcfirst(String str) {
		String newStr = "";
		newStr += str.charAt(0);
		newStr = newStr.toLowerCase();

		return newStr + str.substring(1);
	}

	// 파일이 존재하는지
	public static boolean isFileExists(String filePath) {
		File f = new File(filePath);
		if (f.isFile()) {
			return true;
		}

		return false;
	}

	// 파일내용 읽어오기
	public static String getFileContents(String filePath) {
		String rs = null;
		try {
			// 바이트 단위로 파일읽기
			FileInputStream fileStream = null; // 파일 스트림

			fileStream = new FileInputStream(filePath);// 파일 스트림 생성
			// 버퍼 선언
			byte[] readBuffer = new byte[fileStream.available()];
			while (fileStream.read(readBuffer) != -1) {
			}

			rs = new String(readBuffer);

			fileStream.close(); // 스트림 닫기
		} catch (Exception e) {
			e.getStackTrace();
		}

		return rs;
	}

	// 파일 쓰기
	public static void writeFileContents(String filePath, String contents) {
		BufferedOutputStream bs = null;
		try {
			bs = new BufferedOutputStream(new FileOutputStream(filePath));
			bs.write(contents.getBytes()); // Byte형으로만 넣을 수 있음
		} catch (Exception e) {
			e.getStackTrace();
		} finally {
			try {
				bs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// Json안에 있는 내용을 가져오기
	public static Object getObjectFromJson(String filePath, Class cls) {
		ObjectMapper om = new ObjectMapper();
		Object obj = null;
		try {
			obj = om.readValue(new File(filePath), cls);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {
			e.printStackTrace();
		}

		return obj;
	}

	public static void writeJsonFile(String filePath, Object obj) {
		ObjectMapper om = new ObjectMapper();
		try {
			om.writeValue(new File(filePath), obj);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void makeDir(String dirPath) {
		File dir = new File(dirPath);
		if (!dir.exists()) {
			dir.mkdir();
		}
	}
}