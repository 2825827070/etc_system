# -*- coding: utf-8 -*-
"""
区县中心坐标配置
用于ETC交通流量地理位置映射
"""

# 区县中心点配置
DISTRICT_CENTERS = {
    # === 徐州市辖区 ===
    '徐州市': {
        'center': (34.2061, 117.1851),  # 徐州市政府
        'color': '#FFD700',
        'brightness': 1.0,
        'type': 'city'
    },
    '铜山区': {
        'center': (34.1800, 117.1500),
        'color': '#FFA500',
        'brightness': 0.9,
        'type': 'district'
    },
    '铜山县': {  # 别名
        'center': (34.1800, 117.1500),
        'color': '#FFA500',
        'brightness': 0.9,
        'type': 'district'
    },
    '鼓楼区': {
        'center': (34.2900, 117.1900),
        'color': '#FFA500',
        'brightness': 0.9,
        'type': 'district'
    },
    '云龙区': {
        'center': (34.2500, 117.2500),
        'color': '#FFA500',
        'brightness': 0.9,
        'type': 'district'
    },
    '贾汪区': {
        'center': (34.4400, 117.4600),
        'color': '#FFA500',
        'brightness': 0.9,
        'type': 'district'
    },
    '泉山区': {
        'center': (34.2400, 117.1900),
        'color': '#FFA500',
        'brightness': 0.9,
        'type': 'district'
    },
    
    # === 徐州市县级市/县 ===
    '新沂市': {
        'center': (34.3600, 118.3500),
        'color': '#FF8C00',
        'brightness': 0.85,
        'type': 'county'
    },
    '邳州市': {
        'center': (34.3300, 118.0100),
        'color': '#FF8C00',
        'brightness': 0.85,
        'type': 'county'
    },
    '沛县': {
        'center': (34.7200, 116.9400),
        'color': '#FF8C00',
        'brightness': 0.85,
        'type': 'county'
    },
    '睢宁县': {
        'center': (33.9100, 117.9500),
        'color': '#FF8C00',
        'brightness': 0.85,
        'type': 'county'
    },
    '丰县': {
        'center': (34.7000, 116.6000),
        'color': '#FF8C00',
        'brightness': 0.85,
        'type': 'county'
    },
    
    # === 省界区域（苏皖）===
    '安徽宿州': {
        'center': (33.6361, 116.9846),
        'color': '#696969',
        'brightness': 0.3,
        'type': 'border',
        'province': 'anhui'
    },
    '安徽淮北': {
        'center': (33.9717, 116.7949),
        'color': '#696969',
        'brightness': 0.3,
        'type': 'border',
        'province': 'anhui'
    },
    
    # === 省界区域（苏鲁）===
    '山东枣庄': {
        'center': (34.8564, 117.3237),
        'color': '#696969',
        'brightness': 0.3,
        'type': 'border',
        'province': 'shandong'
    },
    '山东济宁': {
        'center': (35.4154, 116.5870),
        'color': '#696969',
        'brightness': 0.3,
        'type': 'border',
        'province': 'shandong'
    },
    
    # 跨省标识（用于无法识别的跨省卡口）
    '跨省': {
        'center': (34.0000, 117.0000),
        'color': '#808080',
        'brightness': 0.5,
        'type': 'border',
        'province': 'unknown'
    }
}


def get_district_coordinates(district_name):
    """
    获取区县坐标（带小范围随机偏移）
    
    Args:
        district_name: 区县名称
        
    Returns:
        dict: {
            'latitude': float,
            'longitude': float,
            'district': str,
            'color': str,
            'brightness': float,
            'is_border': bool
        }
    """
    import random
    
    # 查找区县配置
    if district_name not in DISTRICT_CENTERS:
        # 尝试模糊匹配
        for key in DISTRICT_CENTERS.keys():
            if district_name in key or key in district_name:
                district_name = key
                break
        else:
            # 默认徐州市中心
            district_name = '徐州市'
    
    config = DISTRICT_CENTERS[district_name]
    center_lat, center_lon = config['center']
    
    # 随机偏移 ±0.02度（约2km）模拟同区县内不同卡口
    offset_lat = random.uniform(-0.02, 0.02)
    offset_lon = random.uniform(-0.02, 0.02)
    
    return {
        'latitude': center_lat + offset_lat,
        'longitude': center_lon + offset_lon,
        'district': district_name,
        'color': config['color'],
        'brightness': config.get('brightness', 1.0),
        'is_border': config.get('type') == 'border',
        'type': config.get('type', 'city')
    }


def get_all_districts():
    """获取所有区县列表"""
    return list(DISTRICT_CENTERS.keys())


def get_xuzhou_districts():
    """获取徐州市辖区和县市"""
    return [name for name, config in DISTRICT_CENTERS.items() 
            if config.get('type') in ['city', 'district', 'county']]


def get_border_regions():
    """获取省界区域"""
    return [name for name, config in DISTRICT_CENTERS.items() 
            if config.get('type') == 'border']


if __name__ == '__main__':
    # 测试
    print("徐州市辖区和县市:")
    for district in get_xuzhou_districts():
        coords = get_district_coordinates(district)
        print(f"  {district}: ({coords['latitude']:.4f}, {coords['longitude']:.4f})")
    
    print("\n省界区域:")
    for region in get_border_regions():
        coords = get_district_coordinates(region)
        print(f"  {region}: ({coords['latitude']:.4f}, {coords['longitude']:.4f})")
    
    print("\n测试随机偏移:")
    for i in range(3):
        coords = get_district_coordinates('徐州市')
        print(f"  第{i+1}次: ({coords['latitude']:.4f}, {coords['longitude']:.4f})")
