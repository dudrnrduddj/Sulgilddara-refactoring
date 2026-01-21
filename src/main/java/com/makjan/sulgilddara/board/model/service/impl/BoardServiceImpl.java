package com.makjan.sulgilddara.board.model.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makjan.sulgilddara.board.common.config.BoardTagUtil;
import com.makjan.sulgilddara.board.model.mapper.BoardMapper;
import com.makjan.sulgilddara.board.model.service.BoardService;
import com.makjan.sulgilddara.board.model.vo.Board;
import com.makjan.sulgilddara.board.model.vo.BoardFile;
import com.makjan.sulgilddara.board.model.vo.BoardPagination;
import com.makjan.sulgilddara.board.model.vo.BoardReply;
import com.makjan.sulgilddara.board.model.vo.BoardReplyUser;
import com.makjan.sulgilddara.board.model.vo.BoardTag;
import com.makjan.sulgilddara.board.model.vo.SearchLiquor;
import com.makjan.sulgilddara.common.utility.Util;
import com.makjan.sulgilddara.model.vo.Pagination;

@Service
public class BoardServiceImpl implements BoardService{
	private BoardMapper bMapper;
	
	@Autowired
	public BoardServiceImpl(BoardMapper bMapper) {
		this.bMapper = bMapper;
	}

	@Override
	public Board selectOne(Integer boardNo) {
		Board board = bMapper.selectBoardOne(boardNo);
		return board;
	}
	
	// selectBoardList 오버로딩 - 키워드검색
	@Override
	public Map<String, Object> selectBoardList(Integer currentPage, String searchKeyword, String searchCondition, String orderSelectBox) {
		
		int totalCount = bMapper.getTotalCount(searchCondition, searchKeyword);
		BoardPagination pn = new BoardPagination(totalCount, currentPage);
		int limit = pn.getBoardLimit();
		int offset = (currentPage-1) * limit ;
		RowBounds rowBounds = new RowBounds(offset, limit);
		List<Board> bList = bMapper.selectBoardList(searchCondition, searchKeyword, orderSelectBox ,rowBounds);
		Map<String, Object> map = new HashMap<>();
		map.put("bList", bList);
		map.put("pn", pn);
		return map;
	}
	
	

	@Override
	public int insertBoard(Board board) {
		int result = bMapper.insertBoard(board);
		return result;
	}

	@Override
	public int updateBoard(Board board) {
		int result = bMapper.updateBoard(board);
		return result;
	}

	@Override
	@Transactional
	public int deleteBoard(Integer boardNo) {
		List<BoardFile> bFileList = bMapper.selectBoardFileListByNo(boardNo);
		int result = bMapper.deleteBoard(boardNo);
		// 실제 파일 삭제
		if(bFileList != null && !bFileList.isEmpty()) {
			for(BoardFile file : bFileList) {
				Path uploadFilePath = Paths.get("C:\\uploadFile\\board\\", file.getBoardFileRename());
				try {
					Files.deleteIfExists(uploadFilePath);
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}
		}
		
		return result;
	}

	@Override
	public int insertTag(BoardTag boardTag) {
		int result = bMapper.insertTag(boardTag);
		return result;
	}

	

	@Override
	public int deleteTag(Integer boardNo) {
		int result = bMapper.deleteTag(boardNo);
		return result;
	}


	@Override
	public int insertBoardFile(BoardFile boardFile) {
		int result = bMapper.insertBoardFile(boardFile);
		return result;
	}

	
	@Override
	public List<BoardTag> selectBoardTagList() {
		List<BoardTag> bTagList = bMapper.selectBoardTagList();
		return bTagList;
	}

	@Override
	public List<BoardFile> selectBoardFileList() {
		List<BoardFile> bFileList = bMapper.selectBoardFileList();
		return bFileList;
	}

	
	// 게시글 NO 조회 - 태그 선택 ( 간편 검색 )
	@Override
	public List<Integer> selectBoardNoByTags(List<String> tagList, Integer tagCount) {
		List<Integer> boardNos = bMapper.selectBoardByTags(tagList, tagCount);
		return boardNos;
	}

	// 태그선택 - No조회 후 No로 board검색 ( 간편 검색)
	@Override
	public Map<String, Object> selectBoardsByBoardNos(Integer currentPage, List<Integer> boardNos, String orderSelectBox) {
		int totalCount = boardNos.size();
		BoardPagination pn = new BoardPagination(totalCount, currentPage);
		int limit = pn.getBoardLimit();
		int offset = (currentPage-1) * limit ;
		RowBounds rowBounds = new RowBounds(offset, limit);
		List<Board> bList = bMapper.selectBoardsByBoardNos(boardNos, rowBounds, orderSelectBox);
		Map<String, Object> map = new HashMap<>();
		map.put("bList", bList);
		map.put("pn", pn);
		return map;
	}

	// 태그 중복 X 전체 조회
	@Override
	public List<BoardTag> selectBoardTagListDistinct(int limit) {
		List<BoardTag> bTagList = bMapper.selectBoardTagListDistinct();
		bTagList = bTagList.size() > limit ? bTagList.subList(0, limit) : bTagList;
		return bTagList;
	}

	@Override
	public int updateBoardFile(BoardFile boardFile) {
		int result = bMapper.updateBoardFile(boardFile);
		return result;
	}

	
	@Override
	public int insertBoardReply(BoardReply boardReply) {
		int result = bMapper.insertBoardReply(boardReply);
		return result;
	}

	@Override
	public List<BoardReply> selectBoardReplyList(Integer boardNo) {
		List<BoardReply> replyList = bMapper.selectBoardReplyList(boardNo);
		return replyList;
	}

	@Override
	public int deleteReply(Integer replyNo) {
		int result = bMapper.deleteReply(replyNo);
		return result;
	}

	@Override
	public int updateReply(Integer replyNo, String replyContent) {
		int result = bMapper.updateReply(replyNo, replyContent);
		return result;
	}

	@Override
	public int increaseViewCount(Integer boardNo) {
		int result = bMapper.increaseViewCount(boardNo);
		return result;
	}

	@Override
	public List<SearchLiquor> searchLiquorList(String liquorName) {
		List<SearchLiquor> searchLiquorResult = bMapper.searchLiquorList(liquorName);
		return searchLiquorResult;
	}

	@Override
	public int getMinBoardNo() {
		int minBoardNo = bMapper.getMinBoardNo();
		return minBoardNo;
	}

	@Override
	public int getMaxBoardNo() {
		int maxBoardNo = bMapper.getMaxBoardNo();
		return maxBoardNo;
	}

	@Override
	public List<BoardReplyUser> selectBoardReplyUser(Integer boardNo) {
		List<BoardReplyUser> boardReplyUserList = bMapper.selectBoardReplyUser(boardNo);
		return boardReplyUserList;
	}

	@Override
	public List<Board> selectBoardListByLiquorId(Integer liquorId) {
		List<Board> bList = bMapper.selectBoardListByLiquorId(liquorId);
		return bList;
	}

	
	
	@Override
	public List<BoardFile> selectBoardFileListByNo(Integer boardNo) {
		List<BoardFile> bFileList = bMapper.selectBoardFileListByNo(boardNo);
		return bFileList;
	}

	@Override
	public List<BoardTag> selectBoardTagByNo(Integer boardNo) {
		List<BoardTag> bTagList = bMapper.selectBoardTagListByNo(boardNo);
		return bTagList;
	}

	@Override
	public Board selectPrevBoard(Integer boardNo) {
		Board prevBoard = bMapper.selectPrevBoard(boardNo);
		return prevBoard;
	}

	@Override
	public Board selectNextBoard(Integer boardNo) {
		Board nextBoard = bMapper.selectNextBoard(boardNo);
		return nextBoard;
	}

	@Override
	public List<BoardTag> selectTagListByBoardNos(List<Integer> boardNos) {
		List<BoardTag> bTagList = bMapper.selectTagListByBoardNos(boardNos);
		return bTagList;
	}

	@Override
	public List<BoardFile> selectFileListByBoardNos(List<Integer> boardNos) {
		List<BoardFile> bTagList = bMapper.selectFileListByBoardNos(boardNos);
		return bTagList;
	}

	
	@Override
	@Transactional
	public int insertBoardInfo(Board board, BoardTag boardTag, MultipartFile uploadFile, String tempIds, String fileOwner) throws IllegalStateException, IOException {
		// 게시글 등록
		int result = bMapper.insertBoard(board);
		
		// 파일 저장
		if(uploadFile != null && !"".equals(uploadFile.getOriginalFilename())) { // null체크시 실제 저장된 이름이 있는지도 체크해줘야 함.
			String fileName = uploadFile.getOriginalFilename();
			String fileRename = Util.fileRename(fileName);
			String isTumbnail = "Y";
			
			// Web용 경로
			String filePath = "/board-images/"; // 이 경로가 fileConfig에 의해 실제 C드라이브 경로로 매핑됨.
			
			uploadFile.transferTo(new File("C:\\uploadFile\\board\\" + fileRename));
			
			BoardFile boardFile = new BoardFile(fileName, fileRename, filePath, board.getBoardNo(), null, isTumbnail, fileOwner);
			
			int boardFileResult = bMapper.insertBoardFile(boardFile);
		}
		
		// 임시파일리스트
		List<String> tempIdList = Arrays.asList(tempIds.split(","));
		int uResult = bMapper.updateTempIds(tempIdList, board.getBoardNo());
		
		
		
		// 태그 등록
		// boardTagName엔 배열 문자열이 저장이 된다.
		// json형식의 문자열을 파싱해주고 배열에 저장
		
		if(!boardTag.getBoardTagName().equals("")) {
			List<String> tagNameArr = BoardTagUtil.boardTagListing(boardTag);
			
			// 각 배열의 값을 하나씩 table에 insert 시켜주기
			for(String tagName : tagNameArr) {
				BoardTag newBoardTag = new BoardTag();
				newBoardTag.setBoardTagName(tagName);
				newBoardTag.setBoardNo(board.getBoardNo());
				int tagResult = bMapper.insertTag(newBoardTag);			
			}
		}
		
		return result;
	}

	
	@Override
	public Map<String, String> insertTempBoardFile(MultipartFile file, String fileOwner) {
		
		String fileName = file.getOriginalFilename();
		String fileRename = Util.fileRename(fileName);
		String isTumbnail = "N";
		// Web용 경로
		String filePath = "/board-images/"; // 이 경로가 fileConfig에 의해 실제 C드라이브 경로로 매핑됨.
		
		try {
			file.transferTo(new File("C:\\uploadFile\\board\\" + fileRename));
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}
		
		String tempId = UUID.randomUUID().toString();
		BoardFile boardFile = new BoardFile(fileName, fileRename, filePath, null, tempId, isTumbnail, fileOwner);
		
		int tempResult = bMapper.insertTempBoardFile(boardFile);
		
	
		Map<String, String> data = new HashMap<>();
	    data.put("fullPath", filePath+fileRename);
	    data.put("tempId", tempId);
		
		return data;
	}

	@Override
	@Transactional
	public int updateBoardInfo(Board board, BoardTag boardTag, MultipartFile reloadFile, String uploadFileRename, String tempIds, String editorImages, String fileOwner) {
		
		int boardResult = bMapper.updateBoard(board);
		
		if(reloadFile != null && !"".equals(reloadFile.getOriginalFilename())) { // null체크시 실제 저장된 이름이 있는지도 체크해줘야 함.
			
			String fileName = reloadFile.getOriginalFilename();
			String fileRename = Util.fileRename(fileName);
			String isTumbnail = "Y";
			
			// Web용 경로
			String filePath = "/board-images/"; // 이 경로가 fileConfig에 의해 실제 C드라이브 경로로 매핑됨.
			
			try {
				reloadFile.transferTo(new File("C:\\uploadFile\\board\\" + fileRename));
				BoardFile boardFile = new BoardFile(fileName, fileRename, filePath, board.getBoardNo(), null, isTumbnail, fileOwner);
				int boardFileResult = bMapper.updateBoardFile(boardFile);				
				// 실제 파일 삭제
				Path uploadFilePath = Paths.get("C:\\uploadFile\\board\\", uploadFileRename);
				Files.deleteIfExists(uploadFilePath);			
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
			}
		}
		
		// 임시 파일리스트
		List<String> tempIdList = Arrays.asList(tempIds.split(","));
		if(!tempIdList.isEmpty()) {
			int uResult = bMapper.updateTempIds(tempIdList, board.getBoardNo());			
		}
		
		// 썸머노트 업로드 이미지 업데이트(삭제)
		List<String> editorImageList = Arrays.asList(editorImages.split(","));
		
		// 1. 기존 DB에 저장된 editor 이미지 조회
	    List<BoardFile> dbImageList = bMapper.selectEditorFilesByBoardNo(board.getBoardNo());
	    
	    // 2. DB에는 있는데 editor에는 없는 이미지 → 삭제
	    for(BoardFile dbFile : dbImageList) {
	    	if (!editorImageList.contains(dbFile.getBoardFileRename())) {
	    		// DB 삭제
	            bMapper.deleteBoardFileByNo(dbFile.getBoardFileNo());
	            // 실제 파일 삭제
	            Path uploadFilePath = Paths.get("C:\\uploadFile\\board\\", dbFile.getBoardFileRename());
	            try {
	                Files.deleteIfExists(uploadFilePath);
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	            
	    	}
	    }
	    
	    // 3. 사용되지 않은 temp 파일 삭제
	    List<BoardFile> unusedTempFiles = bMapper.selectUnusedTempFiles(fileOwner);
	    for(BoardFile file : unusedTempFiles) {
	    	 Path filePath = Paths.get("C:\\uploadFile\\board\\", file.getBoardFileRename());
	         try {
				Files.deleteIfExists(filePath);
				bMapper.deleteBoardFileByNo(file.getBoardFileNo());
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    
		if(!boardTag.getBoardTagName().equals("")) {
			List<String> tagNameArr = BoardTagUtil.boardTagListing(boardTag);
			
			int dResult = bMapper.deleteTag(board.getBoardNo());
			// 각 배열의 값을 하나씩 table에 insert 시켜주기
			for(String tagName : tagNameArr) {
				BoardTag newBoardTag = new BoardTag();
				newBoardTag.setBoardTagName(tagName);
				newBoardTag.setBoardNo(board.getBoardNo());
				int iResult = bMapper.insertTag(newBoardTag);			
			}
		}
		
		return 0;
	}

	
	
	
	
}
