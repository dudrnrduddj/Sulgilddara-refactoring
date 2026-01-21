package com.makjan.sulgilddara.board.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.makjan.sulgilddara.board.model.service.BoardService;
import com.makjan.sulgilddara.board.model.vo.Board;
import com.makjan.sulgilddara.board.model.vo.BoardFile;
import com.makjan.sulgilddara.board.model.vo.BoardReply;
import com.makjan.sulgilddara.board.model.vo.BoardReplyUser;
import com.makjan.sulgilddara.board.model.vo.BoardTag;
import com.makjan.sulgilddara.board.model.vo.SearchLiquor;

import jakarta.servlet.http.HttpSession;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Controller
public class BoardController {
	
	private BoardService bService;
	
	@Autowired
	public BoardController(BoardService bService) {
		this.bService = bService;
	}
	
	// 카드 - 목록 이동 및 검색
	@GetMapping("/board/listCard")
	public String showBoardListCard(@RequestParam(name = "searchMethod", required = false, defaultValue = "keywordSearch") String searchMethod,
			@RequestParam(name="searchKeyword", required = false ) String searchKeyword,
			@RequestParam(name="searchCondition", required = false, defaultValue = "all") String searchCondition,
			@RequestParam(name="orderSelectBox", required = false, defaultValue = "latest") String orderSelectBox,
			@RequestParam(value = "cp", required = false, defaultValue = "1") Integer currentPage,
			Model model) {
		
		// 서비스에서 Pagination 객체, 조회된 bList 객체 매핑해서 반환
		Map<String, Object> map = bService.selectBoardList(currentPage, searchKeyword, searchCondition, orderSelectBox);

		List<Board> bList = (List<Board>) map.get("bList");
		
		List<Integer> boardNos = bList.stream()
                .map(Board::getBoardNo)
                .collect(Collectors.toList());

		// 페이지별 태그 리스트
		List<BoardTag> bTagList = bService.selectTagListByBoardNos(boardNos);
		// 페이지별 파일 리스트
		List<BoardFile> bFileList = bService.selectFileListByBoardNos(boardNos);
		// 태그 중복X 리스트 조회 - 상위 30개
		List<BoardTag> bTagListDistinct = bService.selectBoardTagListDistinct(30);		
		
		model.addAttribute("bList", map.get("bList"));
		model.addAttribute("pn", map.get("pn"));
		model.addAttribute("bFileList", bFileList);
		model.addAttribute("bTagList", bTagList);
		model.addAttribute("bTagListDistinct", bTagListDistinct);
		
		model.addAttribute("searchKeyword", searchKeyword);
		model.addAttribute("searchCondition", searchCondition);
		model.addAttribute("orderSelectBox", orderSelectBox);
		model.addAttribute("searchMethod", searchMethod);
		
		return "board/boardList_card";
	}

	// 상세페이지 이동 - viewStatus는 a태그로 이동 시 조회수 증가할 수 있도록 하는 기준
	@GetMapping("/board/detailPage/{boardNo}/{viewStatus}")
	public String showBoardOne(@PathVariable("boardNo") Integer boardNo,
			@PathVariable(name = "viewStatus") String viewStatus,
			Model model, HttpSession session) {
		
		if(session.getAttribute("userId") == null) {
			return "redirect:/user/login";
		}
		
		if("yes".equals(viewStatus)) {
			// 조회수 증가
			int result = bService.increaseViewCount(boardNo);
			return "redirect:/board/detailPage/"+boardNo+"/no";
		}
		
		// board 
		Board board = bService.selectOne(boardNo);
		Board prevBoard = bService.selectPrevBoard(boardNo);
		Board nextBoard = bService.selectNextBoard(boardNo);
		
		// 게시글No로 해당 게시글 이미지파일 조회
		List<BoardFile> bFileList = bService.selectBoardFileListByNo(boardNo);
		// 게시글 No로 해당 게시글 태그 조회
		List<BoardTag> bTagList = bService.selectBoardTagByNo(boardNo);
		
		model.addAttribute("board", board);
		model.addAttribute("prevBoard", prevBoard);
		model.addAttribute("nextBoard", nextBoard);
		
		model.addAttribute("bFileList", bFileList);
		model.addAttribute("bTagList", bTagList);
		return "board/boardDetail";
	}
	
	// 글 작성 페이지 이동
	@GetMapping("/board/writePage")
	public String showWriteBoardForm(HttpSession session) {
		if(session.getAttribute("userId") == null) {
			return "redirect:/user/login";
		}
		
		return "board/boardWrite";
	}
	
	// 글작성 - post
	@PostMapping("/board/write")
	public String boardWrite(
			@ModelAttribute("Board") Board board,
			@ModelAttribute("BoardTag") BoardTag boardTag,
			@RequestParam(name="uploadFile", required = false) MultipartFile uploadFile,
			@RequestParam("tempIds") String tempIds,
			Model model, HttpSession session) throws IllegalStateException, IOException {
			
		String userId = null;
		String fileOwner = null;
		
		if(session.getAttribute("userId") == null) {
			return "redirect:/user/login";
		}else {
			userId = (String)session.getAttribute("userId");
			fileOwner = userId;
		}
		
		board.setBoardWriter(userId);
		board.setUserId(userId);
		
		// 트랜잭션 처리
		bService.insertBoardInfo(board, boardTag, uploadFile, tempIds, fileOwner);
		
		return "redirect:/board/listCard";
	}

	// 글 수정 페이지 이동
	@GetMapping("/board/modifyPage/{boardNo}")
	public String showModifyForm(@PathVariable("boardNo") Integer boardNo, Model model) {
		// board 
		Board board = bService.selectOne(boardNo);
			
		// 게시글No로 해당 게시글 이미지파일 조회
		List<BoardFile> bFileList = bService.selectBoardFileListByNo(boardNo);
		// 게시글 No로 해당 게시글 태그 조회
		List<BoardTag> bTagList = bService.selectBoardTagByNo(boardNo);		
			
		model.addAttribute("board", board);
		model.addAttribute("bFileList", bFileList);
		model.addAttribute("bTagList", bTagList);
		
		return "board/boardModify";
	}
	
	
	// 글 삭제
	@GetMapping("/board/delete")
	public String boardDelete(@RequestParam("boardNo") Integer boardNo, Model model){
		
		int result = bService.deleteBoard(boardNo);
		
		return "redirect:/board/listCard";
	}
	
	
	
	// 글 수정
	@PostMapping("/board/modify")
	public String boardModify(@ModelAttribute("Board") Board board,
			@ModelAttribute("BoardTag") BoardTag boardTag,
			@RequestParam(name="reloadFile", required = false) MultipartFile reloadFile,
			@RequestParam(name="uploadFileRename", required = false) String uploadFileRename,
			@RequestParam("tempIds") String tempIds,
			@RequestParam("editorImages") String editorImages,
			Model model, HttpSession session) throws IllegalStateException, IOException {
		
		String userId = null;
		String fileOwner = null;
		if(session.getAttribute("userId") == null) {
			return "redirect:/user/login";
		}else {
			userId = (String)session.getAttribute("userId");
			fileOwner = userId;
		}
		
		board.setBoardWriter(userId);
		board.setUserId(userId);
		
		/// 개선 후 코드
		int updateResult = bService.updateBoardInfo(board, boardTag, reloadFile, uploadFileRename, tempIds, editorImages, fileOwner);
		
		return "redirect:/board/detailPage/"+board.getBoardNo()+"/no";
	}
	

	
	// 파일 업로드 메소드 (ajax 통신)
	@ResponseBody
	@PostMapping("/board/uploadImage")
	public ResponseEntity<?> boardUploadImage(@RequestParam("file") MultipartFile file, HttpSession session){
		
		String fileOwner = (String)session.getAttribute("userId");
		
		if(file != null) {
			Map<String, String> data = bService.insertTempBoardFile(file, fileOwner);
			
			return ResponseEntity.ok(data); // 경로 전달
				
		}
		return null;
	}
	
	
	// 간편 검색 - a태그 페이지 이동 (페이지네이션)
	@GetMapping("/board/searchTag_listCard")
	public String getSearchTagListCard(@RequestParam(name = "searchMethod", required = false, defaultValue = "tagSearch") String searchMethod,
			@RequestParam(name = "orderSelectBox", required = false, defaultValue = "latest") String orderSelectBox, 
			@RequestParam("tagName") String tags,
			@RequestParam(value = "cp", required = false, defaultValue = "1") Integer currentPage,
			Model model) {
			
		List<String> tagList = Arrays.asList(tags.split(","));
		int tagCount = tagList.size();
		List<Integer> boardNosByTag = bService.selectBoardNoByTags(tagList, tagCount); // 해당 태그 boarNo -> 검색된 board 객체
		
		Map<String, Object> map = null;
		if(!boardNosByTag.isEmpty()) {
			// 최종 간편 검색 결과 pn, bList 매핑
			map = bService.selectBoardsByBoardNos(currentPage, boardNosByTag, orderSelectBox);
		}
		
		// 태그 중복X 리스트 조회
		List<BoardTag> bTagListDistinct = bService.selectBoardTagListDistinct(30);
		
		if(map != null) {
			List<Board> bList = (List<Board>) map.get("bList");
			
			List<Integer> boardNos = bList.stream()
					.map(Board::getBoardNo)
					.collect(Collectors.toList());
			
			// 페이지별 태그 리스트
			List<BoardTag> bTagList = bService.selectTagListByBoardNos(boardNos);
			// 페이지별 파일 리스트
			List<BoardFile> bFileList = bService.selectFileListByBoardNos(boardNos);
			
			model.addAttribute("bFileList", bFileList);
			model.addAttribute("bTagList", bTagList);
		}
		
		
		if(map != null) {
			model.addAttribute("bList", map.get("bList"));
			model.addAttribute("pn", map.get("pn"));
		}
		model.addAttribute("bTagListDistinct", bTagListDistinct);
		model.addAttribute("selectedTags", tagList);
		model.addAttribute("orderSelectBox", orderSelectBox);
		model.addAttribute("searchMethod", searchMethod);
		
		return "board/boardList_card";
	}	

	
	// 댓글 등록 메소드
	@ResponseBody
	@PostMapping("/board/replyAdd")
	public String addBoardReply(@ModelAttribute BoardReply boardReply,
			HttpSession session) {
		
		String userId = (String) session.getAttribute("userId");
		
		if("".equals(boardReply.getReplyContent())) {
			return "fail";
		}
		
		boardReply.setUserId(userId);
		boardReply.setReplyWriter(userId);
		
		int result = bService.insertBoardReply(boardReply);
		
		return "success";
	}
	
	
	// 댓글 리스트 조회
	@PostMapping("/board/replyList")
	public String getReplyList(@RequestParam("boardNo") Integer boardNo, Model model,
			HttpSession session) {
		
		String userId = (String) session.getAttribute("userId");
		
		List<BoardReplyUser> boardReplyUserList = bService.selectBoardReplyUser(boardNo);
		
		int replyTotalCount = boardReplyUserList.size();
		
		model.addAttribute("boardReplyUserList", boardReplyUserList);
		model.addAttribute("loginUserId", userId);
		model.addAttribute("replyTotalCount", replyTotalCount);
		
		return "board/boardDetail::#replyListContainer";
	}
	

	
	// 댓글 삭제
	@ResponseBody
	@PostMapping("/board/replyDelete")
	public String deleteReply(@RequestParam("replyNo") Integer replyNo) {
		int result = bService.deleteReply(replyNo);
		
		if(result > 0) {
		    return "success";
		} else {
		    return "fail";
		}
	}
	
	
	
	// 댓글 수정
	@ResponseBody
	@PostMapping("/board/replyUpdate")
	public String updateReply(@RequestParam("replyNo") Integer replyNo, @RequestParam("replyContent") String replyContent) {
		
		int result = bService.updateReply(replyNo, replyContent);
		
		if(result > 0) {
		    return "success";
		} else {
		    return "fail";
		}
	}
	
	
	// 술 검색 리스트
	@PostMapping("/board/searchLiquor")
	public String searchLiquorList(@RequestParam("liquorName") String liquorName, Model model) {
		List<SearchLiquor> searchLiquorResult = bService.searchLiquorList(liquorName);
		
		model.addAttribute("searchLiquorResult", searchLiquorResult);
		return "board/boardWrite::#searchResultList";
	}
	

}








