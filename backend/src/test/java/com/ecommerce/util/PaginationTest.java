package com.ecommerce.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("分页算法单元测试")
class PaginationTest {

    @Test
    @DisplayName("分页计算 - 第一页")
    void pagination_FirstPage_ShouldReturnCorrectData() {
        List<String> allData = createTestData(100);
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<String> page = createPage(allData, pageable);
        
        assertEquals(0, page.getNumber());
        assertEquals(10, page.getSize());
        assertEquals(100, page.getTotalElements());
        assertEquals(10, page.getTotalPages());
        assertTrue(page.isFirst());
        assertFalse(page.isLast());
        assertTrue(page.hasNext());
        assertFalse(page.hasPrevious());
    }

    @Test
    @DisplayName("分页计算 - 最后一页")
    void pagination_LastPage_ShouldReturnCorrectData() {
        List<String> allData = createTestData(100);
        Pageable pageable = PageRequest.of(9, 10);
        
        Page<String> page = createPage(allData, pageable);
        
        assertEquals(9, page.getNumber());
        assertFalse(page.isFirst());
        assertTrue(page.isLast());
        assertFalse(page.hasNext());
        assertTrue(page.hasPrevious());
    }

    @Test
    @DisplayName("分页计算 - 中间页")
    void pagination_MiddlePage_ShouldReturnCorrectData() {
        List<String> allData = createTestData(100);
        Pageable pageable = PageRequest.of(5, 10);
        
        Page<String> page = createPage(allData, pageable);
        
        assertEquals(5, page.getNumber());
        assertFalse(page.isFirst());
        assertFalse(page.isLast());
        assertTrue(page.hasNext());
        assertTrue(page.hasPrevious());
    }

    @Test
    @DisplayName("分页计算 - 不足一页")
    void pagination_LessThanOnePage_ShouldReturnSinglePage() {
        List<String> allData = createTestData(5);
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<String> page = createPage(allData, pageable);
        
        assertEquals(5, page.getTotalElements());
        assertEquals(1, page.getTotalPages());
        assertEquals(5, page.getContent().size());
        assertTrue(page.isFirst());
        assertTrue(page.isLast());
    }

    @Test
    @DisplayName("分页计算 - 空数据")
    void pagination_EmptyData_ShouldReturnEmptyPage() {
        List<String> allData = new ArrayList<>();
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<String> page = createPage(allData, pageable);
        
        assertEquals(0, page.getTotalElements());
        assertEquals(0, page.getTotalPages());
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    @DisplayName("分页计算 - 不整除情况")
    void pagination_NotDivisible_ShouldCalculateCorrectly() {
        List<String> allData = createTestData(25);
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<String> page = createPage(allData, pageable);
        
        assertEquals(25, page.getTotalElements());
        assertEquals(3, page.getTotalPages()); // 25/10 = 2.5 -> 3页
    }

    @Test
    @DisplayName("分页计算 - 最后一页不满")
    void pagination_LastPageNotFull_ShouldReturnPartialData() {
        List<String> allData = createTestData(25);
        Pageable pageable = PageRequest.of(2, 10);
        
        Page<String> page = createPage(allData, pageable);
        
        assertEquals(5, page.getContent().size()); // 最后一页只有5条
        assertTrue(page.isLast());
    }

    @Test
    @DisplayName("分页偏移量计算")
    void pagination_Offset_ShouldCalculateCorrectly() {
        Pageable page0 = PageRequest.of(0, 10);
        Pageable page1 = PageRequest.of(1, 10);
        Pageable page5 = PageRequest.of(5, 10);
        
        assertEquals(0, page0.getOffset());
        assertEquals(10, page1.getOffset());
        assertEquals(50, page5.getOffset());
    }

    @Test
    @DisplayName("分页 - 不同页大小")
    void pagination_DifferentPageSizes_ShouldCalculateCorrectly() {
        List<String> allData = createTestData(100);
        
        Page<String> page5 = createPage(allData, PageRequest.of(0, 5));
        Page<String> page20 = createPage(allData, PageRequest.of(0, 20));
        Page<String> page50 = createPage(allData, PageRequest.of(0, 50));
        
        assertEquals(20, page5.getTotalPages());
        assertEquals(5, page20.getTotalPages());
        assertEquals(2, page50.getTotalPages());
    }

    private List<String> createTestData(int count) {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            data.add("Item " + i);
        }
        return data;
    }

    private Page<String> createPage(List<String> allData, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allData.size());
        
        List<String> pageContent = start < allData.size() 
                ? allData.subList(start, end) 
                : new ArrayList<>();
        
        return new PageImpl<>(pageContent, pageable, allData.size());
    }
}
